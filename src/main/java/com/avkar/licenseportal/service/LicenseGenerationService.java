package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.license.LicenseGenerator;
import com.avkar.licenseportal.repository.CustomerRepository;
import com.avkar.licenseportal.repository.LicenseRepository;
import com.avkar.licenseportal.repository.ProductRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class LicenseGenerationService {
    private final LicenseFormService licenseFormService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final LicenseRepository licenseRepository;
    private final AesEncryptionService aesEncryptionService;
    private final LicenseGenerator licenseGenerator;
    private final DealerProductPermissionService permissionService;
    private final DealerService dealerService;
    private final CurrentUserContext currentUserContext;
    private final DealerAccessGuard dealerAccessGuard;

    public LicenseGenerationService(
            LicenseFormService licenseFormService,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            LicenseRepository licenseRepository,
            AesEncryptionService aesEncryptionService,
            LicenseGenerator licenseGenerator,
            DealerProductPermissionService permissionService,
            DealerService dealerService,
            CurrentUserContext currentUserContext,
            DealerAccessGuard dealerAccessGuard
    ) {
        this.licenseFormService = licenseFormService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.licenseRepository = licenseRepository;
        this.aesEncryptionService = aesEncryptionService;
        this.licenseGenerator = licenseGenerator;
        this.permissionService = permissionService;
        this.dealerService = dealerService;
        this.currentUserContext = currentUserContext;
        this.dealerAccessGuard = dealerAccessGuard;
    }

    @Transactional
    public License generateAndSave(LicenseGenerateForm form) {
        licenseFormService.validateFormSelections(form);

        Product product = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
        Customer customer = customerRepository.findByIdWithDealer(form.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        User createdBy = currentUserContext.requireUser();
        Dealer dealer = resolveDealer(customer, form.getProductId());

        String productSecret = aesEncryptionService.decryptFromBase64(product.getSecretKeyEnc());
        String licenseKey = licenseGenerator.generate(
                form.getSystemKey(),
                form.getLicenseOwnerDescription(),
                form.getValidUntil(),
                form.isCameraEnabled(),
                productSecret
        );

        License license = new License();
        license.setLicenseKey(licenseKey);
        license.setProduct(product);
        license.setCustomer(customer);
        license.setDealer(dealer);
        license.setCreatedBy(createdBy);
        license.setSystemKey(form.getSystemKey().trim());
        license.setLicenseOwnerDescription(form.getLicenseOwnerDescription());
        license.setCameraEnabled(form.isCameraEnabled());
        license.setValidUntil(form.getValidUntil());
        license.setIsDemo(form.isDemo());
        license.setCreatedAt(LocalDateTime.now());

        return licenseRepository.save(license);
    }

    @Transactional(readOnly = true)
    public License getForCurrentUser(Long licenseId) {
        License license = licenseRepository.findByIdWithDetails(licenseId)
                .orElseThrow(() -> new NoSuchElementException("License not found: " + licenseId));

        if (currentUserContext.isAdmin()) {
            dealerAccessGuard.requireAdmin();
            return license;
        }

        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        if (!license.getDealer().getId().equals(dealerId)) {
            throw new AccessDeniedException("Bu lisansa erişim yetkiniz yok.");
        }
        return license;
    }

    private Dealer resolveDealer(Customer customer, Long productId) {
        if (currentUserContext.isBayi()) {
            permissionService.assertProductPermissionForCurrentBayi(productId);
            return dealerService.getById(dealerAccessGuard.requireCurrentDealerId());
        }

        dealerAccessGuard.requireAdmin();
        if (customer.getCreatedByDealer() == null) {
            throw new IllegalArgumentException(
                    "Admin lisans üretimi için kurumun bağlı bir bayisi olmalıdır.");
        }
        return customer.getCreatedByDealer();
    }
}
