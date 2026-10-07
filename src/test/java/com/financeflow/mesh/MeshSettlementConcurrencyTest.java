package com.financeflow.mesh;

import com.financeflow.mesh.crypto.MeshCryptoService;
import com.financeflow.mesh.dto.DecryptedPaymentPayload;
import com.financeflow.mesh.dto.EncryptedMeshPacketDto;
import com.financeflow.mesh.dto.IngestResponseDto;
import com.financeflow.mesh.entity.MeshSettlementStatus;
import com.financeflow.mesh.service.MeshIdempotencyService;
import com.financeflow.mesh.service.MeshSettlementService;
import com.financeflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MeshSettlementConcurrencyTest {

    @Autowired
    private MeshSettlementService settlementService;

    @Autowired
    private MeshCryptoService cryptoService;

    @Autowired
    private MeshIdempotencyService idempotencyService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        idempotencyService.reset();
    }

    @Test
    @DisplayName("Thundering Herd Test: 30 concurrent bridge nodes uploading the exact same packet must result in exactly 1 SETTLED and 29 DUPLICATE_DROPPED")
    void testConcurrentDuplicateUploadsSettleExactlyOnce() throws Exception {
        long initialTxCount = transactionRepository.count();

        // 1. Generate one legitimate offline mesh transaction
        KeyPair senderKeyPair = MeshCryptoService.generateRsaKeyPair(2048);
        DecryptedPaymentPayload payload = new DecryptedPaymentPayload(
                "payer@mesh.internal",
                "merchant@mesh.internal",
                new BigDecimal("250.00"),
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "Thundering Herd Concurrency Test"
        );

        EncryptedMeshPacketDto packet = cryptoService.encryptPayload(
                payload,
                senderKeyPair.getPrivate(),
                senderKeyPair.getPublic(),
                cryptoService.getServerPublicKey(),
                5
        );

        int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threadCount);

        AtomicInteger settledCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        List<Future<IngestResponseDto>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startGate.await(); // ensure simultaneous execution
                try {
                    return settlementService.processIngestedPacket(packet);
                } finally {
                    finishGate.countDown();
                }
            }));
        }

        // Release all threads simultaneously
        startGate.countDown();
        finishGate.await(10, TimeUnit.SECONDS);

        for (Future<IngestResponseDto> f : futures) {
            IngestResponseDto res = f.get();
            if (res.getStatus() == MeshSettlementStatus.SETTLED) {
                settledCount.incrementAndGet();
            } else if (res.getStatus() == MeshSettlementStatus.DUPLICATE_DROPPED) {
                duplicateCount.incrementAndGet();
            }
        }

        executor.shutdown();

        // Assert exactly one transaction settled and remainder were dropped idempotently
        assertEquals(1, settledCount.get(), "Exactly one thread must settle the transaction!");
        assertEquals(threadCount - 1, duplicateCount.get(), "All other concurrent calls must be dropped as duplicates!");
        assertEquals(initialTxCount + 1, transactionRepository.count(), "Ledger must reflect exactly one debit!");
    }
}
