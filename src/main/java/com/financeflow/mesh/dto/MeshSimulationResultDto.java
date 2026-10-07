package com.financeflow.mesh.dto;

import com.financeflow.mesh.entity.MeshSettlementStatus;
import java.util.List;

public class MeshSimulationResultDto {

    private String packetId;
    private String ciphertextHash;
    private int totalHops;
    private List<String> networkRoute;
    private MeshSettlementStatus finalStatus;
    private Long transactionId;
    private long totalDurationMs;
    private String details;

    public MeshSimulationResultDto() {}

    public MeshSimulationResultDto(String packetId, String ciphertextHash, int totalHops,
                                   List<String> networkRoute, MeshSettlementStatus finalStatus,
                                   Long transactionId, long totalDurationMs, String details) {
        this.packetId = packetId;
        this.ciphertextHash = ciphertextHash;
        this.totalHops = totalHops;
        this.networkRoute = networkRoute;
        this.finalStatus = finalStatus;
        this.transactionId = transactionId;
        this.totalDurationMs = totalDurationMs;
        this.details = details;
    }

    public String getPacketId() { return packetId; }
    public void setPacketId(String packetId) { this.packetId = packetId; }

    public String getCiphertextHash() { return ciphertextHash; }
    public void setCiphertextHash(String ciphertextHash) { this.ciphertextHash = ciphertextHash; }

    public int getTotalHops() { return totalHops; }
    public void setTotalHops(int totalHops) { this.totalHops = totalHops; }

    public List<String> getNetworkRoute() { return networkRoute; }
    public void setNetworkRoute(List<String> networkRoute) { this.networkRoute = networkRoute; }

    public MeshSettlementStatus getFinalStatus() { return finalStatus; }
    public void setFinalStatus(MeshSettlementStatus finalStatus) { this.finalStatus = finalStatus; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
