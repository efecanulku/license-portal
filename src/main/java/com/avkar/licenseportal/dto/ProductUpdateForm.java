package com.avkar.licenseportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductUpdateForm {
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String code;

    private String description;

    /**
     * Optional: boş bırakılırsa mevcut secret korunur.
     */
    private String secret;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}

