package com.avkar.licenseportal.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseGenerationMessagesTest {

    @Test
    void userMessage_returnsIllegalArgumentMessage() {
        assertEquals(
                "Admin lisans üretimi için kurumun bağlı bir bayisi olmalıdır.",
                LicenseGenerationMessages.userMessage(
                        new IllegalArgumentException("Admin lisans üretimi için kurumun bağlı bir bayisi olmalıdır.")
                )
        );
    }

    @Test
    void userMessage_mapsAccessDenied() {
        assertTrue(LicenseGenerationMessages.userMessage(new AccessDeniedException("x")).contains("yetkiniz"));
    }

    @Test
    void userMessage_mapsNotFound() {
        assertTrue(LicenseGenerationMessages.userMessage(new NoSuchElementException()).contains("bulunamadı"));
    }
}
