package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.CustomerForm;
import com.avkar.licenseportal.dto.SimplePageParams;
import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.repository.CustomerRepository;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final DealerService dealerService;
    private final DealerAccessGuard dealerAccessGuard;

    public CustomerService(
            CustomerRepository customerRepository,
            DealerService dealerService,
            DealerAccessGuard dealerAccessGuard
    ) {
        this.customerRepository = customerRepository;
        this.dealerService = dealerService;
        this.dealerAccessGuard = dealerAccessGuard;
    }

    @Transactional(readOnly = true)
    public List<Customer> listForAdmin(Long dealerIdFilter) {
        dealerAccessGuard.requireAdmin();
        return customerRepository.findAllWithFilters(dealerIdFilter, null);
    }

    @Transactional(readOnly = true)
    public Page<Customer> listPageForAdmin(Long dealerIdFilter, SimplePageParams params) {
        dealerAccessGuard.requireAdmin();
        return customerRepository.findAllWithFiltersPage(
                dealerIdFilter,
                params.normalizedQuery(),
                pageable(params)
        );
    }

    @Transactional(readOnly = true)
    public List<Customer> listForCurrentBayi() {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return customerRepository.findByLinkedDealerId(dealerId, null);
    }

    @Transactional(readOnly = true)
    public Page<Customer> listPageForCurrentBayi(SimplePageParams params) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return customerRepository.findByLinkedDealerIdPage(
                dealerId,
                params.normalizedQuery(),
                pageable(params)
        );
    }

    @Transactional(readOnly = true)
    public List<Customer> listForDealer(Long dealerId) {
        dealerAccessGuard.assertBayiOwnsDealer(dealerId);
        return customerRepository.findByLinkedDealerId(dealerId, null);
    }

    @Transactional(readOnly = true)
    public Customer getForAdmin(Long id) {
        dealerAccessGuard.requireAdmin();
        return customerRepository.findByIdWithDealer(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
    }

    @Transactional(readOnly = true)
    public Customer getForCurrentBayi(Long id) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return getForDealer(id, dealerId);
    }

    @Transactional(readOnly = true)
    public Customer getForDealer(Long id, Long dealerId) {
        dealerAccessGuard.assertBayiOwnsDealer(dealerId);
        return customerRepository.findByIdAndLinkedDealerId(id, dealerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found for dealer: " + dealerId));
    }

    @Transactional
    public Customer createForAdmin(CustomerForm form) {
        dealerAccessGuard.requireAdmin();
        Customer customer = new Customer();
        applyForm(customer, form);
        if (form.getCreatedByDealerId() != null) {
            Dealer dealer = dealerService.getById(form.getCreatedByDealerId());
            customer.setCreatedByDealer(dealer);
        }
        customer.setCreatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        syncLinkedDealers(customer, form.getLinkedDealerIds(), form.getCreatedByDealerId());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer createForCurrentBayi(CustomerForm form) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return createForDealer(dealerId, form);
    }

    @Transactional
    public Customer createForDealer(Long dealerId, CustomerForm form) {
        dealerAccessGuard.assertBayiOwnsDealer(dealerId);
        Dealer dealer = dealerService.getById(dealerId);
        Customer customer = new Customer();
        applyForm(customer, form);
        customer.setCreatedByDealer(dealer);
        customer.setCreatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        linkDealer(customer, dealer);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateForAdmin(Long id, CustomerForm form) {
        dealerAccessGuard.requireAdmin();
        Customer customer = getForAdmin(id);
        applyForm(customer, form);
        syncLinkedDealers(customer, form.getLinkedDealerIds(), null);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateForCurrentBayi(Long id, CustomerForm form) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return updateForDealer(id, dealerId, form);
    }

    @Transactional
    public Customer updateForDealer(Long id, Long dealerId, CustomerForm form) {
        Customer customer = getForDealer(id, dealerId);
        applyForm(customer, form);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public boolean isLinkedToDealer(Long customerId, Long dealerId) {
        return customerRepository.existsByIdAndLinkedDealers_Id(customerId, dealerId);
    }

    private void syncLinkedDealers(Customer customer, List<Long> linkedDealerIds, Long alsoLinkCreatorId) {
        Set<Long> ids = new HashSet<>();
        if (linkedDealerIds != null) {
            ids.addAll(linkedDealerIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toSet()));
        }
        if (alsoLinkCreatorId != null) {
            ids.add(alsoLinkCreatorId);
        }
        if (customer.getCreatedByDealer() != null) {
            ids.add(customer.getCreatedByDealer().getId());
        }
        customer.getLinkedDealers().clear();
        for (Long dealerId : ids) {
            linkDealer(customer, dealerService.getById(dealerId));
        }
    }

    private static void linkDealer(Customer customer, Dealer dealer) {
        customer.getLinkedDealers().add(dealer);
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

    private static PageRequest pageable(SimplePageParams params) {
        return PageRequest.of(
                params.getPage(),
                SimplePageParams.PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name")
        );
    }
}
