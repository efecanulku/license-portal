package com.avkar.licenseportal.dto;

import java.util.List;

public class LicenseCustomerOption {
    private final Long id;
    private final String name;
    private final String taxNumber;
    private final List<LicenseDealerOption> dealers;

    public LicenseCustomerOption(Long id, String name, String taxNumber, List<LicenseDealerOption> dealers) {
        this.id = id;
        this.name = name;
        this.taxNumber = taxNumber;
        this.dealers = dealers;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public List<LicenseDealerOption> getDealers() {
        return dealers;
    }
}
