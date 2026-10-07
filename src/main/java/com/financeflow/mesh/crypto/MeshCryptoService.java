package com.financeflow.mesh.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeflow.mesh.dto.DecryptedPaymentPayload;
import com.financeflow.mesh.dto.EncryptedMeshPacketDto;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
public class MeshCryptoService {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final KeyPair serverKeyPair;
    private final ObjectMapper objectMapper;

    public MeshCryptoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.serverKeyPair = generateRsaKeyPair(2048);
    }

    public static KeyPair generateRsaKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize RSA key generator", e);
        }
    }

    public PublicKey getServerPublicKey() {
        return serverKeyPair.getPublic();
    }

    public String getServerPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded());
    }

    public SecretKey decryptAesKey(String base64EncryptedKey) throws GeneralSecurityException {
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(base64EncryptedKey);
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, serverKeyPair.getPrivate());
        byte[] decryptedKey = rsaCipher.doFinal(encryptedKeyBytes);
        return new SecretKeySpec(decryptedKey, "AES");
    }

    public DecryptedPaymentPayload decryptPayload(String base64Ciphertext, SecretKey aesKey, String base64Iv)
            throws Exception {
        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);
        byte[] iv = Base64.getDecoder().decode(base64Iv);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

        byte[] decryptedBytes = aesCipher.doFinal(ciphertext);
        return objectMapper.readValue(decryptedBytes, DecryptedPaymentPayload.class);
    }

    public boolean verifySignature(String canonicalPayload, String base64Signature, String base64SenderPublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64SenderPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey senderPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(senderPublicKey);
            sig.update(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(base64Signature));
        } catch (Exception e) {
            return false;
        }
    }

    public String computeSha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public String canonicalize(DecryptedPaymentPayload payload) {
        return payload.getSenderEmail() + "|" +
               payload.getRecipientEmail() + "|" +
               payload.getAmount().toPlainString() + "|" +
               payload.getNonce() + "|" +
               payload.getTimestamp();
    }

    public String signPayload(String canonicalPayload, PrivateKey privateKey) throws GeneralSecurityException {
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(privateKey);
        sig.update(canonicalPayload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public EncryptedMeshPacketDto encryptPayload(DecryptedPaymentPayload payload,
                                                 PrivateKey senderPrivateKey,
                                                 PublicKey senderPublicKey,
                                                 PublicKey recipientServerPublicKey,
                                                 int ttl) throws Exception {
        // 1. Generate ephemeral AES-256 key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Generate random 12-byte IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        // 3. Encrypt payload with AES-GCM
        byte[] plaintext = objectMapper.writeValueAsBytes(payload);
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        byte[] ciphertext = aesCipher.doFinal(plaintext);

        // 4. Encrypt AES key with Server RSA Public Key (RSA-OAEP)
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, recipientServerPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // 5. Sign the canonical payload with sender private key
        String canonical = canonicalize(payload);
        String signature = signPayload(canonical, senderPrivateKey);

        String packetId = UUID.randomUUID().toString();
        String b64Ciphertext = Base64.getEncoder().encodeToString(ciphertext);
        String b64EncAesKey = Base64.getEncoder().encodeToString(encryptedAesKey);
        String b64Iv = Base64.getEncoder().encodeToString(iv);
        String b64SenderPubKey = Base64.getEncoder().encodeToString(senderPublicKey.getEncoded());

        return new EncryptedMeshPacketDto(
                packetId,
                ttl,
                0,
                System.currentTimeMillis(),
                b64EncAesKey,
                b64Iv,
                b64Ciphertext,
                b64SenderPubKey,
                signature,
                "origin-device"
        );
    }
}
