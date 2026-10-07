package com.financeflow.mesh.service;

import com.financeflow.mesh.crypto.MeshCryptoService;
import com.financeflow.mesh.dto.*;
import com.financeflow.mesh.entity.MeshSettlementStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MeshNetworkSimulator {

    private static final Logger log = LoggerFactory.getLogger(MeshNetworkSimulator.class);

    private final MeshCryptoService cryptoService;
    private final MeshSettlementService settlementService;

    public MeshNetworkSimulator(MeshCryptoService cryptoService, MeshSettlementService settlementService) {
        this.cryptoService = cryptoService;
        this.settlementService = settlementService;
    }

    /**
     * Simulates an offline sender creating a signed and hybrid-encrypted packet,
     * gossiping it across N disconnected peer relay nodes, until a bridge node
     * connects to 4G and submits it to the core backend.
     */
    public MeshSimulationResultDto simulateOfflineMeshRun(MeshSimulationRequestDto req) throws Exception {
        long startTime = System.currentTimeMillis();

        // 1. Generate Sender's on-device RSA KeyPair (simulating mobile secure enclave)
        KeyPair senderKeyPair = MeshCryptoService.generateRsaKeyPair(2048);

        // 2. Sender crafts offline payment payload
        DecryptedPaymentPayload payload = new DecryptedPaymentPayload(
                req.getSenderEmail(),
                req.getRecipientEmail(),
                req.getAmount(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                req.getNote() != null ? req.getNote() : "Offline P2P Mesh Payment"
        );

        // 3. Sender encrypts with Server Public Key & signs with Sender Private Key
        int ttl = Math.max(req.getRelayCount() + 2, 5);
        EncryptedMeshPacketDto packet = cryptoService.encryptPayload(
                payload,
                senderKeyPair.getPrivate(),
                senderKeyPair.getPublic(),
                cryptoService.getServerPublicKey(),
                ttl
        );

        // 4. Simulate Gossip Hops across BLE Mesh
        List<String> route = new ArrayList<>();
        route.add("sender-node [" + req.getSenderEmail() + " - Offline]");

        for (int i = 1; i <= req.getRelayCount(); i++) {
            packet.setHopCount(packet.getHopCount() + 1);
            packet.setTtl(packet.getTtl() - 1);
            route.add("relay-peer-" + i + " [BLE Mesh Hop " + packet.getHopCount() + " - Offline]");
        }

        String bridgeNode = "bridge-node-alpha [4G/Wi-Fi Connected]";
        route.add(bridgeNode);
        packet.setBridgeNodeId(bridgeNode);

        // 5. Bridge uploads packet to Backend Ingestion Pipeline
        IngestResponseDto ingestRes = settlementService.processIngestedPacket(packet);

        long totalDuration = System.currentTimeMillis() - startTime;
        String ciphertextHash = cryptoService.computeSha256Hex(packet.getCiphertext());

        return new MeshSimulationResultDto(
                packet.getPacketId(),
                ciphertextHash,
                packet.getHopCount(),
                route,
                ingestRes.getStatus(),
                ingestRes.getTransactionId(),
                totalDuration,
                ingestRes.getMessage()
        );
    }
}
