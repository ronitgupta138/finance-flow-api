package com.financeflow.mesh.controller;

import com.financeflow.dto.ApiResponse;
import com.financeflow.mesh.crypto.MeshCryptoService;
import com.financeflow.mesh.dto.EncryptedMeshPacketDto;
import com.financeflow.mesh.dto.IngestResponseDto;
import com.financeflow.mesh.dto.MeshSimulationRequestDto;
import com.financeflow.mesh.dto.MeshSimulationResultDto;
import com.financeflow.mesh.entity.MeshSettlementLog;
import com.financeflow.mesh.repository.MeshSettlementLogRepository;
import com.financeflow.mesh.service.MeshNetworkSimulator;
import com.financeflow.mesh.service.MeshSettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mesh")
public class MeshController {

    private final MeshCryptoService cryptoService;
    private final MeshSettlementService settlementService;
    private final MeshNetworkSimulator simulator;
    private final MeshSettlementLogRepository logRepository;

    public MeshController(MeshCryptoService cryptoService,
                          MeshSettlementService settlementService,
                          MeshNetworkSimulator simulator,
                          MeshSettlementLogRepository logRepository) {
        this.cryptoService = cryptoService;
        this.settlementService = settlementService;
        this.simulator = simulator;
        this.logRepository = logRepository;
    }

    /**
     * Endpoint for mobile clients to download and cache the central bank / FinFlow RSA public key
     * while they have active internet connectivity.
     */
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicKey() {
        String keyBase64 = cryptoService.getServerPublicKeyBase64();
        return ResponseEntity.ok(ApiResponse.ok(
                "Server public key retrieved successfully",
                Map.of("publicKey", keyBase64, "algorithm", "RSA-OAEP-2048")
        ));
    }

    /**
     * Production bridge ingestion endpoint: 4G/Wi-Fi connected bridge nodes upload
     * buffered packets here for atomic settlement.
     */
    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<IngestResponseDto>> ingestPacket(
            @RequestBody EncryptedMeshPacketDto packet) {
        IngestResponseDto response = settlementService.processIngestedPacket(packet);
        return ResponseEntity.ok(ApiResponse.ok("Packet ingestion evaluated", response));
    }

    /**
     * Interactive simulation endpoint: Generates an offline sender, simulates N Bluetooth hops,
     * routes to a bridge node, and settles the payment.
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<MeshSimulationResultDto>> runSimulation(
            @RequestBody MeshSimulationRequestDto request) throws Exception {
        MeshSimulationResultDto result = simulator.simulateOfflineMeshRun(request);
        return ResponseEntity.ok(ApiResponse.ok("Offline mesh simulation completed", result));
    }

    /**
     * Audit log endpoint: View recent settlement transactions and deduplication events.
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<MeshSettlementLog>>> getRecentLogs() {
        List<MeshSettlementLog> logs = logRepository.findTop20ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.ok("Recent mesh settlement logs retrieved", logs));
    }
}
