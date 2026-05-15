package com.avkar.licenseportal.dto;

public class DealerLicenseCountDto {
    private final Long dealerId;
    private final String dealerName;
    private final long licenseCount;

    public DealerLicenseCountDto(Long dealerId, String dealerName, Long licenseCount) {
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.licenseCount = licenseCount != null ? licenseCount : 0L;
    }

    public Long getDealerId() {
        return dealerId;
    }

    public String getDealerName() {
        return dealerName;
    }

    public long getLicenseCount() {
        return licenseCount;
    }
}
