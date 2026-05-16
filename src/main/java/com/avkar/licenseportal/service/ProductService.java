package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.ProductCreateForm;
import com.avkar.licenseportal.dto.ProductUpdateForm;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.dto.SimplePageParams;
import com.avkar.licenseportal.repository.ProductRepository;
import com.avkar.licenseportal.repository.UserRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final AesEncryptionService aesEncryptionService;
    private final DealerAccessGuard dealerAccessGuard;
    private final CurrentUserContext currentUserContext;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProductService(
            ProductRepository productRepository,
            AesEncryptionService aesEncryptionService,
            DealerAccessGuard dealerAccessGuard,
            CurrentUserContext currentUserContext,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.productRepository = productRepository;
        this.aesEncryptionService = aesEncryptionService;
        this.dealerAccessGuard = dealerAccessGuard;
        this.currentUserContext = currentUserContext;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Product> listAll() {
        dealerAccessGuard.requireAdmin();
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> listPage(SimplePageParams params) {
        dealerAccessGuard.requireAdmin();
        String q = params.normalizedQuery();
        if (q == null) {
            return productRepository.findAllByOrderByNameAsc(pageable(params));
        }
        return productRepository.searchPage(q, pageable(params));
    }

    public Product getById(Long id) {
        dealerAccessGuard.requireAdmin();
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }

    @Transactional
    public Product create(ProductCreateForm form) {
        dealerAccessGuard.requireAdmin();
        Product p = new Product();
        p.setName(form.getName().trim());
        p.setCode(form.getCode().trim());
        p.setDescription(form.getDescription());
        p.setSecretKeyEnc(aesEncryptionService.encryptToBase64(form.getSecret()));
        p.setActive(Boolean.TRUE);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        try {
            return productRepository.save(p);
        } catch (DataIntegrityViolationException e) {
            // unique(code) vb.
            throw e;
        }
    }

    @Transactional
    public Product update(Long id, ProductUpdateForm form) {
        dealerAccessGuard.requireAdmin();
        Product p = getById(id);
        p.setName(form.getName().trim());
        p.setCode(form.getCode().trim());
        p.setDescription(form.getDescription());
        if (form.getSecret() != null && !form.getSecret().isBlank()) {
            verifyAdminPasswordForSecretChange(form.getAdminPassword());
            p.setSecretKeyEnc(aesEncryptionService.encryptToBase64(form.getSecret()));
        }
        p.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(p);
    }

    @Transactional(readOnly = true)
    public String revealSecret(Long id, String adminPassword) {
        dealerAccessGuard.requireAdmin();
        verifyAdminPasswordForSecretChange(adminPassword);
        Product p = getById(id);
        return aesEncryptionService.decryptFromBase64(p.getSecretKeyEnc());
    }

    @Transactional
    public void toggleActive(Long id) {
        dealerAccessGuard.requireAdmin();
        Product p = getById(id);
        boolean current = Boolean.TRUE.equals(p.getActive());
        p.setActive(!current);
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
    }

    private void verifyAdminPasswordForSecretChange(String adminPassword) {
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalArgumentException("Secret değiştirmek için mevcut şifrenizi girin.");
        }
        var user = currentUserContext.requireUser();
        var managed = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı."));
        if (!passwordEncoder.matches(adminPassword, managed.getPasswordHash())) {
            throw new IllegalArgumentException("Mevcut şifre hatalı.");
        }
    }

    private static PageRequest pageable(SimplePageParams params) {
        return PageRequest.of(
                params.getPage(),
                SimplePageParams.PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name")
        );
    }
}

