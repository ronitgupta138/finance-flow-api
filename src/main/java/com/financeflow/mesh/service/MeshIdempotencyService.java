package com.financeflow.mesh.service;

import com.financeflow.mesh.repository.MeshSettlementLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeshIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(MeshIdempotencyService.class);

    private final MeshSettlementLogRepository logRepository;
    private final ConcurrentHashMap<String, Long> inFlightOrSettled = new ConcurrentHashMap<>();

    public MeshIdempotencyService(MeshSettlementLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Atomically attempts to claim execution rights for a given ciphertext digest.
     * Prevents multi-threaded / multi-bridge duplicate settlement races.
     *
     * @param ciphertextHash SHA-256 digest of packet ciphertext
     * @return true if this thread is the unique winner and may proceed to settle; false if duplicate
     */
    public boolean tryClaim(String ciphertextHash) {
        if (ciphertextHash == null || ciphertextHash.isBlank()) {
            return false;
        }

        // Fast-path in-memory atomic CAS
        Long existingClaim = inFlightOrSettled.putIfAbsent(ciphertextHash, System.currentTimeMillis());
        if (existingClaim != null) {
            log.debug("Idempotency lock denied (in-memory collision) for hash: {}", ciphertextHash);
            return false;
        }

        // Secondary durable check in database
        if (logRepository.existsByCiphertextHash(ciphertextHash)) {
            log.debug("Idempotency lock denied (database record exists) for hash: {}", ciphertextHash);
            return false;
        }

        return true;
    }

    /**
     * Release in-memory claim if a non-fatal failure occurs before settlement.
     */
    public void releaseClaim(String ciphertextHash) {
        if (ciphertextHash != null) {
            inFlightOrSettled.remove(ciphertextHash);
        }
    }

    /**
     * Clear in-memory cache (primarily for tests or simulation resets).
     */
    public void reset() {
        inFlightOrSettled.clear();
    }
}
