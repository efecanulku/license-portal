package com.avkar.licenseportal.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class LicenseListFilter {
    private Long dealerId;
    private Long productId;
    private Long customerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntilFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntilTo;

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getValidUntilFrom() {
        return validUntilFrom;
    }

    public void setValidUntilFrom(LocalDate validUntilFrom) {
        this.validUntilFrom = validUntilFrom;
    }

    public LocalDate getValidUntilTo() {
        return validUntilTo;
    }

    public void setValidUntilTo(LocalDate validUntilTo) {
        this.validUntilTo = validUntilTo;
    }
}
