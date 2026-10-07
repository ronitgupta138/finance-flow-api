package com.financeflow.mesh.dto;

import java.math.BigDecimal;

public class MeshSimulationRequestDto {

    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
    private int relayCount = 3;
    private String note;

    public MeshSimulationRequestDto() {}

    public MeshSimulationRequestDto(String senderEmail, String recipientEmail, BigDecimal amount, int relayCount, String note) {
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.amount = amount;
        this.relayCount = relayCount;
        this.note = note;
    }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getRelayCount() { return relayCount; }
    public void setRelayCount(int relayCount) { this.relayCount = relayCount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
