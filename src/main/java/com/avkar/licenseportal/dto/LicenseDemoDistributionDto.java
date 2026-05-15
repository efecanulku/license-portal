package com.avkar.licenseportal.dto;

public class LicenseDemoDistributionDto {
    private final long demoCount;
    private final long productionCount;

    public LicenseDemoDistributionDto(long demoCount, long productionCount) {
        this.demoCount = demoCount;
        this.productionCount = productionCount;
    }

    public long getDemoCount() {
        return demoCount;
    }

    public long getProductionCount() {
        return productionCount;
    }

    public long getTotal() {
        return demoCount + productionCount;
    }

    public int getDemoPercent() {
        long total = getTotal();
        if (total == 0) {
            return 0;
        }
        return (int) Math.round(100.0 * demoCount / total);
    }
}
