package com.financeflow.mesh.dto;

public class EncryptedMeshPacketDto {

    private String packetId;
    private int ttl = 5;
    private int hopCount = 0;
    private long createdAt;
    private String encryptedAesKey;
    private String iv;
    private String ciphertext;
    private String senderPublicKey;
    private String signature;
    private String bridgeNodeId;

    public EncryptedMeshPacketDto() {}

    public EncryptedMeshPacketDto(String packetId, int ttl, int hopCount, long createdAt,
                                  String encryptedAesKey, String iv, String ciphertext,
                                  String senderPublicKey, String signature, String bridgeNodeId) {
        this.packetId = packetId;
        this.ttl = ttl;
        this.hopCount = hopCount;
        this.createdAt = createdAt;
        this.encryptedAesKey = encryptedAesKey;
        this.iv = iv;
        this.ciphertext = ciphertext;
        this.senderPublicKey = senderPublicKey;
        this.signature = signature;
        this.bridgeNodeId = bridgeNodeId;
    }

    public String getPacketId() { return packetId; }
    public void setPacketId(String packetId) { this.packetId = packetId; }

    public int getTtl() { return ttl; }
    public void setTtl(int ttl) { this.ttl = ttl; }

    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getEncryptedAesKey() { return encryptedAesKey; }
    public void setEncryptedAesKey(String encryptedAesKey) { this.encryptedAesKey = encryptedAesKey; }

    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }

    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }

    public String getSenderPublicKey() { return senderPublicKey; }
    public void setSenderPublicKey(String senderPublicKey) { this.senderPublicKey = senderPublicKey; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getBridgeNodeId() { return bridgeNodeId; }
    public void setBridgeNodeId(String bridgeNodeId) { this.bridgeNodeId = bridgeNodeId; }
}
