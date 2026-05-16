package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.CustomerLicenseSummaryDto;
import com.avkar.licenseportal.dto.DealerLicenseCountDto;
import com.avkar.licenseportal.dto.ExpiringLicenseFilter;
import com.avkar.licenseportal.dto.LicenseDemoDistributionDto;
import com.avkar.licenseportal.dto.LicenseExpiryStatus;
import com.avkar.licenseportal.dto.ProductLicenseCountDto;
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
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        List<DealerLicenseCountDto> rows = licenseRepository.countLicensesByDealerForBayiDealer(dealerId);
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
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return searchExpiring(null, dealerId, filter);
    }

    @Transactional(readOnly = true)
    public List<ProductLicenseCountDto> productLicenseCountsForAdmin() {
        dealerAccessGuard.requireAdmin();
        return licenseRepository.countLicensesByProductForAdmin();
    }

    @Transactional(readOnly = true)
    public List<CustomerLicenseSummaryDto> customerLicenseSummaryForAdmin() {
        dealerAccessGuard.requireAdmin();
        return licenseRepository.summarizeLicensesByCustomerForAdmin();
    }

    @Transactional(readOnly = true)
    public List<CustomerLicenseSummaryDto> customerLicenseSummaryForCurrentBayi() {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return licenseRepository.summarizeLicensesByCustomerForBayiDealer(dealerId);
    }

    @Transactional(readOnly = true)
    public LicenseDemoDistributionDto demoDistributionForAdmin() {
        dealerAccessGuard.requireAdmin();
        return new LicenseDemoDistributionDto(
                licenseRepository.countDemoLicensesForAdmin(),
                licenseRepository.countProductionLicensesForAdmin()
        );
    }

    @Transactional(readOnly = true)
    public LicenseDemoDistributionDto demoDistributionForCurrentBayi() {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        long demo = licenseRepository.countDemoLicensesForBayiDealer(dealerId);
        long production = licenseRepository.countProductionLicensesForBayiDealer(dealerId);
        return new LicenseDemoDistributionDto(demo, production);
    }

    @Transactional(readOnly = true)
    public long countExpiringSoonForAdmin(int withinDays) {
        dealerAccessGuard.requireAdmin();
        return countExpiringLicenses(null, null, withinDays);
    }

    @Transactional(readOnly = true)
    public long countExpiringSoonForCurrentBayi(int withinDays) {
        Long dealerId = dealerAccessGuard.requireCurrentDealerId();
        return countExpiringLicenses(null, dealerId, withinDays);
    }

    private long countExpiringLicenses(Long userId, Long dealerId, int withinDays) {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(withinDays);
        return licenseRepository.searchExpiringLicenses(
                userId,
                dealerId,
                today,
                until,
                LicenseExpiryStatus.ALL.name(),
                PageRequest.of(0, 1)
        ).getTotalElements();
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
