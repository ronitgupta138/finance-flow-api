package com.financeflow.mesh.service;

import com.financeflow.entity.*;
import com.financeflow.mesh.crypto.MeshCryptoService;
import com.financeflow.mesh.dto.DecryptedPaymentPayload;
import com.financeflow.mesh.dto.EncryptedMeshPacketDto;
import com.financeflow.mesh.dto.IngestResponseDto;
import com.financeflow.mesh.entity.MeshSettlementLog;
import com.financeflow.mesh.entity.MeshSettlementStatus;
import com.financeflow.mesh.repository.MeshSettlementLogRepository;
import com.financeflow.repository.CategoryRepository;
import com.financeflow.repository.TransactionRepository;
import com.financeflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class MeshSettlementService {

    private static final Logger log = LoggerFactory.getLogger(MeshSettlementService.class);
    private static final long MAX_PACKET_AGE_MS = 24 * 60 * 60 * 1000L; // 24 hours max packet age

    private final MeshCryptoService cryptoService;
    private final MeshIdempotencyService idempotencyService;
    private final MeshSettlementLogRepository settlementLogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public MeshSettlementService(MeshCryptoService cryptoService,
                                 MeshIdempotencyService idempotencyService,
                                 MeshSettlementLogRepository settlementLogRepository,
                                 UserRepository userRepository,
                                 CategoryRepository categoryRepository,
                                 TransactionRepository transactionRepository) {
        this.cryptoService = cryptoService;
        this.idempotencyService = idempotencyService;
        this.settlementLogRepository = settlementLogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Main ingestion pipeline executed when a bridge node posts an encrypted packet.
     * Guaranteed idempotent across high-concurrency multi-bridge uploads.
     */
    @Transactional
    public IngestResponseDto processIngestedPacket(EncryptedMeshPacketDto packet) {
        long startTime = System.currentTimeMillis();

        if (packet == null || packet.getCiphertext() == null || packet.getPacketId() == null) {
            return IngestResponseDto.failure("null", "Malformed packet payload", System.currentTimeMillis() - startTime);
        }

        // 1. Time-To-Live & Age Guard
        long ageMs = System.currentTimeMillis() - packet.getCreatedAt();
        if (packet.getTtl() <= 0 || ageMs > MAX_PACKET_AGE_MS) {
            log.warn("Packet {} rejected: expired TTL ({}) or excessive age ({} ms)", packet.getPacketId(), packet.getTtl(), ageMs);
            return IngestResponseDto.expired(packet.getPacketId(), System.currentTimeMillis() - startTime);
        }

        // 2. Compute cryptographic ciphertext hash (the idempotency key)
        String ciphertextHash = cryptoService.computeSha256Hex(packet.getCiphertext());

        // 3. High-concurrency atomic CAS claim
        boolean claimWon = idempotencyService.tryClaim(ciphertextHash);
        if (!claimWon) {
            log.info("Duplicate packet detected! Packet: {}, Hash: {} -> short-circuiting as DUPLICATE_DROPPED",
                    packet.getPacketId(), ciphertextHash);
            return IngestResponseDto.duplicate(packet.getPacketId(), System.currentTimeMillis() - startTime);
        }

        try {
            // 4. Hybrid Decryption: Recover AES session key using Server Private Key
            SecretKey aesKey = cryptoService.decryptAesKey(packet.getEncryptedAesKey());

            // 5. Decrypt payload using AES-256-GCM (verifying AEAD authentication tag)
            DecryptedPaymentPayload payload = cryptoService.decryptPayload(packet.getCiphertext(), aesKey, packet.getIv());

            // 6. Verify Digital Signature of the Sender
            String canonicalPayload = cryptoService.canonicalize(payload);
            boolean validSignature = cryptoService.verifySignature(
                    canonicalPayload, packet.getSignature(), packet.getSenderPublicKey());

            if (!validSignature) {
                log.error("Signature verification failed for packet {} (Sender: {})", packet.getPacketId(), payload.getSenderEmail());
                recordSettlementLog(packet, ciphertextHash, payload, MeshSettlementStatus.INVALID_SIGNATURE, "Signature verification failed");
                return IngestResponseDto.invalidSignature(packet.getPacketId(), System.currentTimeMillis() - startTime);
            }

            // 7. Atomic Core Ledger Settle
            User sender = getOrCreateUser(payload.getSenderEmail(), "Sender User");
            User recipient = getOrCreateUser(payload.getRecipientEmail(), "Recipient User");
            Category category = getOrCreateMeshCategory(sender);

            Transaction tx = new Transaction(
                    payload.getAmount(),
                    TransactionType.EXPENSE,
                    "Offline UPI Payment to " + payload.getRecipientEmail() + " (" + payload.getNote() + ")",
                    LocalDate.now(),
                    category,
                    sender
            );
            Transaction savedTx = transactionRepository.save(tx);

            // 8. Record audit log
            recordSettlementLog(packet, ciphertextHash, payload, MeshSettlementStatus.SETTLED, null);

            long latency = System.currentTimeMillis() - startTime;
            log.info("Successfully settled offline mesh payment! Packet: {}, TxID: {}, Amount: {}, Latency: {} ms",
                    packet.getPacketId(), savedTx.getId(), payload.getAmount(), latency);

            return IngestResponseDto.settled(packet.getPacketId(), savedTx.getId(), latency);

        } catch (Exception ex) {
            log.error("Failed to settle packet {}: {}", packet.getPacketId(), ex.getMessage(), ex);
            idempotencyService.releaseClaim(ciphertextHash); // Release in-memory claim on unexpected error
            return IngestResponseDto.failure(packet.getPacketId(), "Decryption or processing error: " + ex.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    private User getOrCreateUser(String email, String defaultName) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        User newUser = new User(email, "offline-mesh-dummy-pass", defaultName, Role.ROLE_USER);
        return userRepository.save(newUser);
    }

    private Category getOrCreateMeshCategory(User user) {
        return categoryRepository.findAllByUserId(user.getId()).stream()
                .filter(c -> "Offline UPI".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category("Offline UPI", "#0891b2", BigDecimal.valueOf(50000), user)));
    }

    private void recordSettlementLog(EncryptedMeshPacketDto packet, String ciphertextHash,
                                     DecryptedPaymentPayload payload, MeshSettlementStatus status, String failureReason) {
        MeshSettlementLog logEntry = new MeshSettlementLog(
                packet.getPacketId(),
                ciphertextHash,
                payload != null ? payload.getSenderEmail() : "unknown",
                payload != null ? payload.getRecipientEmail() : "unknown",
                payload != null ? payload.getAmount() : BigDecimal.ZERO,
                packet.getHopCount(),
                status,
                packet.getBridgeNodeId()
        );
        logEntry.setFailureReason(failureReason);
        settlementLogRepository.save(logEntry);
    }
}
