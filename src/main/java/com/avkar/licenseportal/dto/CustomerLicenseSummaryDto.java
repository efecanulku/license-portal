package com.avkar.licenseportal.dto;

public class CustomerLicenseSummaryDto {
    private final Long customerId;
    private final String customerName;
    private final long licenseCount;

    public CustomerLicenseSummaryDto(Long customerId, String customerName, Long licenseCount) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.licenseCount = licenseCount != null ? licenseCount : 0L;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public long getLicenseCount() {
        return licenseCount;
    }
}
