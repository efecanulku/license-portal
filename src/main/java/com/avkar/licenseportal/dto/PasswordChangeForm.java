package com.avkar.licenseportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeForm {
    @NotBlank(message = "Mevcut şifre zorunludur.")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre zorunludur.")
    @Size(min = 8, message = "Yeni şifre en az 8 karakter olmalıdır.")
    private String newPassword;

    @NotBlank(message = "Şifre tekrarı zorunludur.")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
