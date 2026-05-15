package com.avkar.licenseportal.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BAYI izolasyonu: servis katmanında dealerId filtresi ve yetki kontrolü (döküman §6).
 */
@Service
public class DealerAccessGuard {
    private final CurrentUserContext currentUserContext;

    public DealerAccessGuard(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public Long requireCurrentDealerId() {
        return currentUserContext.requireDealerId();
    }

    @Transactional(readOnly = true)
    public void requireAdmin() {
        if (!currentUserContext.isAdmin()) {
            throw new AccessDeniedException("Admin yetkisi gerekli.");
        }
    }

    /**
     * BAYI ise yalnızca kendi dealerId'si ile işlem yapabilir. Admin bu kontrolü atlar (admin endpoint'leri ayrı korunur).
     */
    @Transactional(readOnly = true)
    public void assertBayiOwnsDealer(Long dealerId) {
        if (currentUserContext.isAdmin()) {
            return;
        }
        Long currentDealerId = requireCurrentDealerId();
        if (!currentDealerId.equals(dealerId)) {
            throw new AccessDeniedException("Bu bayi kaydına erişim yetkiniz yok.");
        }
    }

    @Transactional(readOnly = true)
    public void requireBayi() {
        if (!currentUserContext.isBayi()) {
            throw new AccessDeniedException("Bayi yetkisi gerekli.");
        }
    }
}
