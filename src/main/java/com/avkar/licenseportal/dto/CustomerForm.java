package com.avkar.licenseportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CustomerForm {
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String taxNumber;

    private String address;

    @Size(max = 255)
    private String contactName;

    @Email
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 50)
    private String contactPhone;

    /** Sadece admin oluştururken: ilk oluşturan bayi (opsiyonel). */
    private Long createdByDealerId;

    /** Admin düzenlemede: kuruma bağlı bayiler (çoklu). */
    private List<Long> linkedDealerIds = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getCreatedByDealerId() {
        return createdByDealerId;
    }

    public void setCreatedByDealerId(Long createdByDealerId) {
        this.createdByDealerId = createdByDealerId;
    }

    public List<Long> getLinkedDealerIds() {
        return linkedDealerIds;
    }

    public void setLinkedDealerIds(List<Long> linkedDealerIds) {
        this.linkedDealerIds = linkedDealerIds != null ? linkedDealerIds : new ArrayList<>();
    }
}
