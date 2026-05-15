package com.avkar.licenseportal.license;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseGeneratorTest {

    private final LicenseGenerator generator = new LicenseGenerator();

    @Test
    void generate_isDeterministicForSameInputs() {
        LocalDate until = LocalDate.of(2027, 5, 15);
        String a = generator.generate("SYS-1", "Oda 1", until, true, "secret-abc");
        String b = generator.generate("SYS-1", "Oda 1", until, true, "secret-abc");
        assertEquals(a, b);
        assertTrue(a.contains("-"));
    }

    @Test
    void generate_changesWhenInputsChange() {
        LocalDate until = LocalDate.of(2027, 5, 15);
        String a = generator.generate("SYS-1", "Oda 1", until, true, "secret-abc");
        String b = generator.generate("SYS-2", "Oda 1", until, true, "secret-abc");
        assertNotEquals(a, b);
    }

    @Test
    void generate_rejectsBlankSystemKey() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.generate("  ", "x", LocalDate.now().plusDays(1), false, "secret"));
    }

    @Test
    void generate_rejectsBlankProductSecret() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.generate("SYS", "x", LocalDate.now().plusDays(1), false, ""));
    }
}
