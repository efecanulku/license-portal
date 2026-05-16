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
import com.avkar.licenseportal.util.CustomerDealerResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
        Dealer dealer = resolveDealer(customer, form);

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

    private Dealer resolveDealer(Customer customer, LicenseGenerateForm form) {
        if (currentUserContext.isBayi()) {
            permissionService.assertProductPermissionForCurrentBayi(form.getProductId());
            return dealerService.getById(dealerAccessGuard.requireCurrentDealerId());
        }

        dealerAccessGuard.requireAdmin();
        List<Dealer> eligible = CustomerDealerResolver.eligibleDealers(customer);
        if (eligible.isEmpty()) {
            throw new IllegalArgumentException(
                    "Kurumun bağlı bayisi yok. Kurum düzenlemeden en az bir bayi bağlayın.");
        }
        if (eligible.size() == 1) {
            return eligible.get(0);
        }
        Long dealerId = form.getDealerId();
        if (dealerId == null) {
            throw new IllegalArgumentException("Bu kuruma birden fazla bayi bağlı; lütfen bayi seçin.");
        }
        return eligible.stream()
                .filter(d -> dealerId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seçilen bayi bu kuruma bağlı değil."));
    }
}
