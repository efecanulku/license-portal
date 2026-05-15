package com.avkar.licenseportal.license;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Base64;

/**
 * Lisans anahtarı üretimi (döküman §5).
 * Şirket tarafından farklı bir algoritma verilirse bu sınıf güncellenir.
 */
@Component
public class LicenseGenerator {

    /**
     * @param systemKey Kurulum makinesinden alınan sistem anahtarı
     * @param ownerDescription Lisans sahibi açıklaması (opsiyonel)
     * @param validUntil Geçerlilik bitiş tarihi
     * @param cameraEnabled Kamera parametresi
     * @param productSecret Ürün secret (AES decrypt sonrası düz metin)
     * @return Üretilen lisans anahtarı
     */
    public String generate(
            String systemKey,
            String ownerDescription,
            LocalDate validUntil,
            boolean cameraEnabled,
            String productSecret
    ) {
        if (systemKey == null || systemKey.isBlank()) {
            throw new IllegalArgumentException("systemKey is required");
        }
        if (validUntil == null) {
            throw new IllegalArgumentException("validUntil is required");
        }
        if (productSecret == null || productSecret.isBlank()) {
            throw new IllegalArgumentException("productSecret is required");
        }

        String payload = String.join("\n",
                systemKey.trim(),
                ownerDescription == null ? "" : ownerDescription.trim(),
                validUntil.toString(),
                cameraEnabled ? "1" : "0"
        );

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(productSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(signature);

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            return formatLicenseKey(encoded);
        } catch (Exception e) {
            throw new IllegalStateException("License generation failed", e);
        }
    }

    private static String formatLicenseKey(String encoded) {
        String compact = encoded.length() > 32 ? encoded.substring(0, 32) : encoded;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < compact.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append('-');
            }
            sb.append(compact.charAt(i));
        }
        return sb.toString().toUpperCase();
    }
}
