package com.avkar.licenseportal.service;

import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.entity.DealerProductPermission;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.DealerProductPermissionRepository;
import com.avkar.licenseportal.repository.ProductRepository;
import com.avkar.licenseportal.repository.UserRepository;
import com.avkar.licenseportal.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DealerProductPermissionService {
    private final DealerProductPermissionRepository permissionRepository;
    private final ProductRepository productRepository;
    private final DealerService dealerService;
    private final UserRepository userRepository;

    public DealerProductPermissionService(
            DealerProductPermissionRepository permissionRepository,
            ProductRepository productRepository,
            DealerService dealerService,
            UserRepository userRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.productRepository = productRepository;
        this.dealerService = dealerService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DealerProductPermission> listForDealer(Long dealerId) {
        ensureDealerExists(dealerId);
        return permissionRepository.findByDealerIdWithDetails(dealerId);
    }

    @Transactional(readOnly = true)
    public List<Product> listGrantableProducts(Long dealerId) {
        ensureDealerExists(dealerId);
        Set<Long> grantedProductIds = permissionRepository.findByDealerIdWithDetails(dealerId).stream()
                .map(p -> p.getProduct().getId())
                .collect(Collectors.toSet());

        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> !grantedProductIds.contains(p.getId()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    @Transactional
    public DealerProductPermission grant(Long dealerId, Long productId) {
        Dealer dealer = dealerService.getById(dealerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("Inactive product cannot be granted");
        }
        if (permissionRepository.existsByDealer_IdAndProduct_Id(dealerId, productId)) {
            throw new IllegalArgumentException("Permission already exists");
        }

        DealerProductPermission permission = new DealerProductPermission();
        permission.setDealer(dealer);
        permission.setProduct(product);
        permission.setGrantedAt(LocalDateTime.now());
        SecurityUtils.getCurrentUser(userRepository).ifPresent(permission::setGrantedBy);
        return permissionRepository.save(permission);
    }

    @Transactional
    public void revoke(Long dealerId, Long productId) {
        ensureDealerExists(dealerId);
        if (!permissionRepository.existsByDealer_IdAndProduct_Id(dealerId, productId)) {
            throw new NoSuchElementException("Permission not found for dealer=" + dealerId + " product=" + productId);
        }
        permissionRepository.deleteByDealer_IdAndProduct_Id(dealerId, productId);
    }

    private void ensureDealerExists(Long dealerId) {
        dealerService.getById(dealerId);
    }
}
