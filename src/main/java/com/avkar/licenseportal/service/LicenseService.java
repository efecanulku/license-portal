package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.LicenseListFilter;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.entity.Product;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.LicenseRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LicenseService {
    private final LicenseRepository licenseRepository;
    private final CurrentUserContext currentUserContext;
    private final DealerAccessGuard dealerAccessGuard;

    public LicenseService(
            LicenseRepository licenseRepository,
            CurrentUserContext currentUserContext,
            DealerAccessGuard dealerAccessGuard
    ) {
        this.licenseRepository = licenseRepository;
        this.currentUserContext = currentUserContext;
        this.dealerAccessGuard = dealerAccessGuard;
    }

    @Transactional(readOnly = true)
    public List<License> listForAdmin(LicenseListFilter filter) {
        dealerAccessGuard.requireAdmin();
        return licenseRepository.searchWithFilters(
                filter.getDealerId(),
                filter.getProductId(),
                filter.getCustomerId(),
                filter.getValidUntilFrom(),
                filter.getValidUntilTo()
        );
    }

    @Transactional(readOnly = true)
    public List<License> listForCurrentBayi(LicenseListFilter filter) {
        User user = currentUserContext.requireUser();
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return licenseRepository.searchForBayiUser(
                user.getId(),
                dealerId,
                filter.getProductId(),
                filter.getCustomerId(),
                filter.getValidUntilFrom(),
                filter.getValidUntilTo()
        );
    }

    /**
     * Bayi liste filtresi: tabloda görünen ürünler (kendi ürettikleri).
     */
    @Transactional(readOnly = true)
    public List<Product> listFilterProductsForCurrentBayi() {
        dealerAccessGuard.requireCurrentDealerId();
        return licenseRepository.findDistinctProductsByCreatedByUserId(
                currentUserContext.requireUser().getId()
        );
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
        Long userId = currentUserContext.requireUser().getId();
        if (!license.getDealer().getId().equals(dealerId)
                || !license.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Bu lisansa erişim yetkiniz yok.");
        }
        return license;
    }
}
