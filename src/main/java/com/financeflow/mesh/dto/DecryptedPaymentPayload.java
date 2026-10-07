package com.financeflow.mesh.dto;

import java.math.BigDecimal;

public class DecryptedPaymentPayload {

    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
    private String nonce;
    private long timestamp;
    private String note;

    public DecryptedPaymentPayload() {}

    public DecryptedPaymentPayload(String senderEmail, String recipientEmail, BigDecimal amount,
                                   String nonce, long timestamp, String note) {
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.amount = amount;
        this.nonce = nonce;
        this.timestamp = timestamp;
        this.note = note;
    }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
