package com.financeflow.mesh.dto;

import com.financeflow.mesh.entity.MeshSettlementStatus;

public class IngestResponseDto {

    private String packetId;
    private MeshSettlementStatus status;
    private String message;
    private Long transactionId;
    private long executionTimeMs;

    public IngestResponseDto() {}

    public IngestResponseDto(String packetId, MeshSettlementStatus status, String message,
                             Long transactionId, long executionTimeMs) {
        this.packetId = packetId;
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.executionTimeMs = executionTimeMs;
    }

    public static IngestResponseDto duplicate(String packetId, long latency) {
        return new IngestResponseDto(packetId, MeshSettlementStatus.DUPLICATE_DROPPED,
                "Packet was already processed or is currently settling (idempotent short-circuit)", null, latency);
    }

    public static IngestResponseDto expired(String packetId, long latency) {
        return new IngestResponseDto(packetId, MeshSettlementStatus.EXPIRED,
                "Packet exceeded allowed Time-To-Live (TTL) or timestamp window", null, latency);
    }

    public static IngestResponseDto invalidSignature(String packetId, long latency) {
        return new IngestResponseDto(packetId, MeshSettlementStatus.INVALID_SIGNATURE,
                "Cryptographic signature check failed or payload tampered", null, latency);
    }

    public static IngestResponseDto failure(String packetId, String msg, long latency) {
        return new IngestResponseDto(packetId, MeshSettlementStatus.FAILED, msg, null, latency);
    }

    public static IngestResponseDto settled(String packetId, Long txId, long latency) {
        return new IngestResponseDto(packetId, MeshSettlementStatus.SETTLED,
                "Transaction successfully settled in core ledger", txId, latency);
    }

    public String getPacketId() { return packetId; }
    public void setPacketId(String packetId) { this.packetId = packetId; }

    public MeshSettlementStatus getStatus() { return status; }
    public void setStatus(MeshSettlementStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
