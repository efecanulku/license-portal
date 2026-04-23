package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.ProductCreateForm;
import com.avkar.licenseportal.dto.ProductUpdateForm;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final AesEncryptionService aesEncryptionService;

    public ProductService(ProductRepository productRepository, AesEncryptionService aesEncryptionService) {
        this.productRepository = productRepository;
        this.aesEncryptionService = aesEncryptionService;
    }

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }

    @Transactional
    public Product create(ProductCreateForm form) {
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
        Product p = getById(id);
        p.setName(form.getName().trim());
        p.setCode(form.getCode().trim());
        p.setDescription(form.getDescription());
        if (form.getSecret() != null && !form.getSecret().isBlank()) {
            p.setSecretKeyEnc(aesEncryptionService.encryptToBase64(form.getSecret()));
        }
        p.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(p);
    }

    @Transactional
    public void toggleActive(Long id) {
        Product p = getById(id);
        boolean current = Boolean.TRUE.equals(p.getActive());
        p.setActive(!current);
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
    }
}

