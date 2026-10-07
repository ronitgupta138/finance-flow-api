package com.financeflow.mesh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mesh_settlement_logs", indexes = {
    @Index(name = "idx_mesh_ciphertext_hash", columnList = "ciphertextHash", unique = true),
    @Index(name = "idx_mesh_packet_id", columnList = "packetId")
})
public class MeshSettlementLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String packetId;

    @Column(nullable = false, unique = true, length = 64)
    private String ciphertextHash;

    @Column(length = 100)
    private String senderEmail;

    @Column(length = 100)
    private String recipientEmail;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer hopCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeshSettlementStatus status = MeshSettlementStatus.SETTLED;

    @Column(length = 100)
    private String bridgeNodeId;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime settledAt;

    public MeshSettlementLog() {}

    public MeshSettlementLog(String packetId, String ciphertextHash, String senderEmail,
                             String recipientEmail, BigDecimal amount, Integer hopCount,
                             MeshSettlementStatus status, String bridgeNodeId) {
        this.packetId = packetId;
        this.ciphertextHash = ciphertextHash;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.amount = amount;
        this.hopCount = hopCount != null ? hopCount : 0;
        this.status = status;
        this.bridgeNodeId = bridgeNodeId;
        this.createdAt = LocalDateTime.now();
        if (status == MeshSettlementStatus.SETTLED) {
            this.settledAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPacketId() { return packetId; }
    public void setPacketId(String packetId) { this.packetId = packetId; }

    public String getCiphertextHash() { return ciphertextHash; }
    public void setCiphertextHash(String ciphertextHash) { this.ciphertextHash = ciphertextHash; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getHopCount() { return hopCount; }
    public void setHopCount(Integer hopCount) { this.hopCount = hopCount; }

    public MeshSettlementStatus getStatus() { return status; }
    public void setStatus(MeshSettlementStatus status) { this.status = status; }

    public String getBridgeNodeId() { return bridgeNodeId; }
    public void setBridgeNodeId(String bridgeNodeId) { this.bridgeNodeId = bridgeNodeId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
}
