package com.avkar.licenseportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
public class EncryptionConfig {

    @Bean
    public SecretKey licensePortalMasterKey(Environment env) {
        // local'da `.env` Spring property olarak gelir; prod'da environment variable olarak beklenir
        String raw = env.getProperty("LICENSE_PORTAL_MASTER_KEY");
        if (!StringUtils.hasText(raw)) {
            raw = System.getenv("LICENSE_PORTAL_MASTER_KEY");
        }
        if (!StringUtils.hasText(raw)) {
            raw = readDotEnvValue("LICENSE_PORTAL_MASTER_KEY");
        }
        if (!StringUtils.hasText(raw)) {
            throw new IllegalStateException("Missing required env var: LICENSE_PORTAL_MASTER_KEY");
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(raw.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AES master key", e);
        }
    }

    private static String readDotEnvValue(String key) {
        Path path = Paths.get(System.getProperty("user.dir", "."), ".env");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String prefix = key + "=";
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (trimmed.startsWith(prefix)) {
                    return trimmed.substring(prefix.length()).trim();
                }
            }
            return null;
        } catch (IOException ignored) {
            return null;
        }
    }
}

