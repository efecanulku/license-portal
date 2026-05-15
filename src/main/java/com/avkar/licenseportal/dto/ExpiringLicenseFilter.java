package com.avkar.licenseportal.dto;

public class ExpiringLicenseFilter {
    public static final int DEFAULT_WITHIN_DAYS = 30;
    public static final int PAGE_SIZE = 20;

    private LicenseExpiryStatus status = LicenseExpiryStatus.ALL;
    private int withinDays = DEFAULT_WITHIN_DAYS;
    private int page = 0;

    public LicenseExpiryStatus getStatus() {
        return status;
    }

    public void setStatus(LicenseExpiryStatus status) {
        this.status = status != null ? status : LicenseExpiryStatus.ALL;
    }

    public int getWithinDays() {
        return withinDays;
    }

    public void setWithinDays(int withinDays) {
        this.withinDays = withinDays > 0 ? withinDays : DEFAULT_WITHIN_DAYS;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }
}
