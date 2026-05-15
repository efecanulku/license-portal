package com.avkar.licenseportal.dto;

import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.entity.Product;

import java.util.List;

public class LicenseFormOptions {
    private final List<Product> products;
    private final List<Customer> customers;

    public LicenseFormOptions(List<Product> products, List<Customer> customers) {
        this.products = products;
        this.customers = customers;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
