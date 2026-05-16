package com.avkar.licenseportal.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class LicenseListFilter {
    public static final int PAGE_SIZE = 20;

    private Long dealerId;
    private Long productId;
    private Long customerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntilFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntilTo;

    private int page = 0;

    private String q;

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String normalizedQuery() {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
