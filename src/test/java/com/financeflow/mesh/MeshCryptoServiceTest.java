package com.financeflow.mesh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeflow.mesh.crypto.MeshCryptoService;
import com.financeflow.mesh.dto.DecryptedPaymentPayload;
import com.financeflow.mesh.dto.EncryptedMeshPacketDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MeshCryptoServiceTest {

    private MeshCryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new MeshCryptoService(new ObjectMapper());
    }

    @Test
    @DisplayName("Should successfully encrypt and decrypt payload via hybrid RSA-OAEP and AES-256-GCM")
    void testHybridEncryptionRoundtrip() throws Exception {
        KeyPair senderKeyPair = MeshCryptoService.generateRsaKeyPair(2048);

        DecryptedPaymentPayload originalPayload = new DecryptedPaymentPayload(
                "alice@mesh.internal",
                "bob@mesh.internal",
                new BigDecimal("499.50"),
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "Offline Coffee Payment"
        );

        EncryptedMeshPacketDto packet = cryptoService.encryptPayload(
                originalPayload,
                senderKeyPair.getPrivate(),
                senderKeyPair.getPublic(),
                cryptoService.getServerPublicKey(),
                5
        );

        assertNotNull(packet.getCiphertext());
        assertNotNull(packet.getEncryptedAesKey());
        assertNotNull(packet.getIv());
        assertNotNull(packet.getSignature());

        // Decrypt AES Key using Server Private Key
        SecretKey recoveredKey = cryptoService.decryptAesKey(packet.getEncryptedAesKey());
        assertNotNull(recoveredKey);

        // Decrypt Payload using recovered AES Key
        DecryptedPaymentPayload decrypted = cryptoService.decryptPayload(packet.getCiphertext(), recoveredKey, packet.getIv());

        assertEquals(originalPayload.getSenderEmail(), decrypted.getSenderEmail());
        assertEquals(originalPayload.getRecipientEmail(), decrypted.getRecipientEmail());
        assertEquals(originalPayload.getAmount(), decrypted.getAmount());
        assertEquals(originalPayload.getNonce(), decrypted.getNonce());

        // Verify Signature
        String canonical = cryptoService.canonicalize(decrypted);
        boolean signatureValid = cryptoService.verifySignature(canonical, packet.getSignature(), packet.getSenderPublicKey());
        assertTrue(signatureValid);
    }

    @Test
    @DisplayName("Should reject signature if payload has been tampered with by intermediate rogue relay")
    void testTamperedPayloadSignatureFails() throws Exception {
        KeyPair senderKeyPair = MeshCryptoService.generateRsaKeyPair(2048);

        DecryptedPaymentPayload originalPayload = new DecryptedPaymentPayload(
                "alice@mesh.internal",
                "bob@mesh.internal",
                new BigDecimal("50.00"),
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "Lunch"
        );

        EncryptedMeshPacketDto packet = cryptoService.encryptPayload(
                originalPayload,
                senderKeyPair.getPrivate(),
                senderKeyPair.getPublic(),
                cryptoService.getServerPublicKey(),
                5
        );

        // Tamper with canonical text: change 50.00 to 5000.00
        String tamperedCanonical = "alice@mesh.internal|bob@mesh.internal|5000.00|" + originalPayload.getNonce() + "|" + originalPayload.getTimestamp();

        boolean signatureValid = cryptoService.verifySignature(tamperedCanonical, packet.getSignature(), packet.getSenderPublicKey());
        assertFalse(signatureValid, "Signature verification must fail on tampered amounts!");
    }
}
