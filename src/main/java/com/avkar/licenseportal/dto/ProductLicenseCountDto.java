package com.avkar.licenseportal.dto;

public class ProductLicenseCountDto {
    private final Long productId;
    private final String productName;
    private final String productCode;
    private final long licenseCount;

    public ProductLicenseCountDto(Long productId, String productName, String productCode, Long licenseCount) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.licenseCount = licenseCount != null ? licenseCount : 0L;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public long getLicenseCount() {
        return licenseCount;
    }
}
