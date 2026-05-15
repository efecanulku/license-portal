package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.CustomerForm;
import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final DealerService dealerService;

    public CustomerService(CustomerRepository customerRepository, DealerService dealerService) {
        this.customerRepository = customerRepository;
        this.dealerService = dealerService;
    }

    @Transactional(readOnly = true)
    public List<Customer> listForAdmin(Long dealerIdFilter) {
        return customerRepository.findAllWithOptionalDealerFilter(dealerIdFilter);
    }

    @Transactional(readOnly = true)
    public List<Customer> listForDealer(Long dealerId) {
        return customerRepository.findByCreatedByDealerId(dealerId);
    }

    @Transactional(readOnly = true)
    public Customer getForAdmin(Long id) {
        return customerRepository.findByIdWithDealer(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
    }

    @Transactional(readOnly = true)
    public Customer getForDealer(Long id, Long dealerId) {
        return customerRepository.findByIdAndCreatedByDealerId(id, dealerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found for dealer: " + dealerId));
    }

    @Transactional
    public Customer createForAdmin(CustomerForm form) {
        Customer customer = new Customer();
        applyForm(customer, form);
        if (form.getCreatedByDealerId() != null) {
            Dealer dealer = dealerService.getById(form.getCreatedByDealerId());
            customer.setCreatedByDealer(dealer);
        }
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer createForDealer(Long dealerId, CustomerForm form) {
        Dealer dealer = dealerService.getById(dealerId);
        Customer customer = new Customer();
        applyForm(customer, form);
        customer.setCreatedByDealer(dealer);
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateForAdmin(Long id, CustomerForm form) {
        Customer customer = getForAdmin(id);
        applyForm(customer, form);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateForDealer(Long id, Long dealerId, CustomerForm form) {
        Customer customer = getForDealer(id, dealerId);
        applyForm(customer, form);
        return customerRepository.save(customer);
    }

    private void applyForm(Customer customer, CustomerForm form) {
        customer.setName(form.getName().trim());
        customer.setTaxNumber(emptyToNull(form.getTaxNumber()));
        customer.setAddress(emptyToNull(form.getAddress()));
        customer.setContactName(emptyToNull(form.getContactName()));
        customer.setContactEmail(emptyToNull(form.getContactEmail()));
        customer.setContactPhone(emptyToNull(form.getContactPhone()));
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
