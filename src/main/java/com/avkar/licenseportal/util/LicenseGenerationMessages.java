package com.avkar.licenseportal.util;

import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;

public final class LicenseGenerationMessages {

    private LicenseGenerationMessages() {
    }

    public static String userMessage(Throwable e) {
        Throwable root = rootCause(e);
        if (root instanceof IllegalArgumentException iae && iae.getMessage() != null && !iae.getMessage().isBlank()) {
            return iae.getMessage();
        }
        if (root instanceof AccessDeniedException) {
            return "Bu ürün veya kurum için yetkiniz yok.";
        }
        if (root instanceof NoSuchElementException) {
            return "Seçilen kurum veya ürün bulunamadı. Sayfayı yenileyip tekrar deneyin.";
        }
        if (root instanceof IllegalStateException) {
            return "Lisans üretilemedi (şifreleme yapılandırması). Yöneticinize başvurun.";
        }
        return "Lisans üretilemedi. Bilgileri kontrol edip tekrar deneyin.";
    }

    private static Throwable rootCause(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
