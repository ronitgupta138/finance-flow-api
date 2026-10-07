package com.financeflow.mesh;

import com.financeflow.mesh.dto.MeshSimulationRequestDto;
import com.financeflow.mesh.dto.MeshSimulationResultDto;
import com.financeflow.mesh.entity.MeshSettlementStatus;
import com.financeflow.mesh.service.MeshNetworkSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MeshNetworkSimulatorTest {

    @Autowired
    private MeshNetworkSimulator simulator;

    @Test
    @DisplayName("Should successfully execute simulated 3-hop gossip payment from offline sender to online bridge")
    void testEndToEndMeshSimulation() throws Exception {
        MeshSimulationRequestDto request = new MeshSimulationRequestDto(
                "basement_user@mesh.internal",
                "ground_shop@mesh.internal",
                new BigDecimal("75.00"),
                3,
                "Chai and Samosa Offline Payment"
        );

        MeshSimulationResultDto result = simulator.simulateOfflineMeshRun(request);

        assertNotNull(result.getPacketId());
        assertNotNull(result.getCiphertextHash());
        assertEquals(3, result.getTotalHops());
        assertEquals(MeshSettlementStatus.SETTLED, result.getFinalStatus());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getNetworkRoute().size() >= 4);
        assertTrue(result.getNetworkRoute().get(0).contains("Offline"));
        assertTrue(result.getNetworkRoute().get(result.getNetworkRoute().size() - 1).contains("Connected"));
    }
}
