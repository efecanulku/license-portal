package com.avkar.licenseportal.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesEncryptionService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptionService(SecretKey licensePortalMasterKey) {
        this.masterKey = licensePortalMasterKey;
    }

    public String encryptToBase64(String plaintext) {
        if (plaintext == null) return null;

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer out = ByteBuffer.allocate(1 + iv.length + ciphertext.length);
            out.put((byte) iv.length);
            out.put(iv);
            out.put(ciphertext);

            return Base64.getEncoder().encodeToString(out.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encrypt failed", e);
        }
    }

    public String decryptFromBase64(String payloadBase64) {
        if (payloadBase64 == null) return null;

        try {
            byte[] payload = Base64.getDecoder().decode(payloadBase64);
            ByteBuffer in = ByteBuffer.wrap(payload);

            int ivLen = Byte.toUnsignedInt(in.get());
            if (ivLen < 12 || ivLen > 32 || in.remaining() < ivLen + 1) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }

            byte[] iv = new byte[ivLen];
            in.get(iv);
            byte[] ciphertext = new byte[in.remaining()];
            in.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decrypt failed", e);
        }
    }
}

