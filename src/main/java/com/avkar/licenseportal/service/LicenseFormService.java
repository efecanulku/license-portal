package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.LicenseFormOptions;
import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.repository.CustomerRepository;
import com.avkar.licenseportal.repository.DealerProductPermissionRepository;
import com.avkar.licenseportal.repository.ProductRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class LicenseFormService {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final DealerProductPermissionRepository permissionRepository;
    private final DealerAccessGuard dealerAccessGuard;
    private final CurrentUserContext currentUserContext;

    public LicenseFormService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            DealerProductPermissionRepository permissionRepository,
            DealerAccessGuard dealerAccessGuard,
            CurrentUserContext currentUserContext
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.permissionRepository = permissionRepository;
        this.dealerAccessGuard = dealerAccessGuard;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public LicenseFormOptions buildOptionsForAdmin() {
        dealerAccessGuard.requireAdmin();
        return new LicenseFormOptions(
                productRepository.findByActiveTrueOrderByNameAsc(),
                customerRepository.findAllWithFilters(null, null)
        );
    }

    @Transactional(readOnly = true)
    public LicenseFormOptions buildOptionsForCurrentBayi() {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return new LicenseFormOptions(
                permissionRepository.findActiveProductsByDealerId(dealerId),
                customerRepository.findByLinkedDealerId(dealerId, null)
        );
    }

    /**
     * Form gönderiminde seçilen ürün/kurumun rol ve yetkiye uygun olduğunu doğrular (Gün 16 üretim öncesi).
     */
    @Transactional(readOnly = true)
    public void validateFormSelections(LicenseGenerateForm form) {
        if (currentUserContext.isBayi()) {
            validateBayiSelections(form);
        } else {
            dealerAccessGuard.requireAdmin();
            validateAdminSelections(form);
        }
    }

    private void validateAdminSelections(LicenseGenerateForm form) {
        Product product = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("Pasif ürün seçilemez.");
        }
        customerRepository.findByIdWithDealer(form.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));
    }

    private void validateBayiSelections(LicenseGenerateForm form) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        if (!permissionRepository.existsByDealer_IdAndProduct_Id(dealerId, form.getProductId())) {
            throw new AccessDeniedException("Bu ürün için yetkiniz yok.");
        }
        if (!customerRepository.existsByIdAndLinkedDealers_Id(form.getCustomerId(), dealerId)) {
            throw new NoSuchElementException("Customer not found for dealer");
        }
    }

}
