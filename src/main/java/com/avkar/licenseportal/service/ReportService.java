package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.DealerLicenseCountDto;
import com.avkar.licenseportal.dto.ExpiringLicenseFilter;
import com.avkar.licenseportal.dto.LicenseExpiryStatus;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.LicenseRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {
    private final LicenseRepository licenseRepository;
    private final DealerService dealerService;
    private final CurrentUserContext currentUserContext;
    private final DealerAccessGuard dealerAccessGuard;

    public ReportService(
            LicenseRepository licenseRepository,
            DealerService dealerService,
            CurrentUserContext currentUserContext,
            DealerAccessGuard dealerAccessGuard
    ) {
        this.licenseRepository = licenseRepository;
        this.dealerService = dealerService;
        this.currentUserContext = currentUserContext;
        this.dealerAccessGuard = dealerAccessGuard;
    }

    @Transactional(readOnly = true)
    public List<DealerLicenseCountDto> dealerLicenseCountsForAdmin() {
        dealerAccessGuard.requireAdmin();
        return licenseRepository.countLicensesByDealerForAdmin();
    }

    @Transactional(readOnly = true)
    public List<DealerLicenseCountDto> dealerLicenseCountsForCurrentBayi() {
        User user = currentUserContext.requireUser();
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        List<DealerLicenseCountDto> rows = licenseRepository.countLicensesByDealerForBayiUser(
                user.getId(), dealerId
        );
        if (!rows.isEmpty()) {
            return rows;
        }
        Dealer dealer = dealerService.getById(dealerId);
        return List.of(new DealerLicenseCountDto(dealer.getId(), dealer.getName(), 0L));
    }

    @Transactional(readOnly = true)
    public Page<License> expiringLicensesForAdmin(ExpiringLicenseFilter filter) {
        dealerAccessGuard.requireAdmin();
        return searchExpiring(null, null, filter);
    }

    @Transactional(readOnly = true)
    public Page<License> expiringLicensesForCurrentBayi(ExpiringLicenseFilter filter) {
        User user = currentUserContext.requireUser();
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return searchExpiring(user.getId(), dealerId, filter);
    }

    private Page<License> searchExpiring(Long userId, Long dealerId, ExpiringLicenseFilter filter) {
        LocalDate today = LocalDate.now();
        int withinDays = filter.getWithinDays();
        LocalDate expiringUntil = today.plusDays(withinDays);
        LicenseExpiryStatus status = filter.getStatus();

        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                ExpiringLicenseFilter.PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "validUntil").and(Sort.by(Sort.Direction.ASC, "id"))
        );

        return licenseRepository.searchExpiringLicenses(
                userId,
                dealerId,
                today,
                expiringUntil,
                status.name(),
                pageable
        );
    }
}
