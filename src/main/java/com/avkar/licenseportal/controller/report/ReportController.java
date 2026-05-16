package com.avkar.licenseportal.controller.report;

import com.avkar.licenseportal.dto.CustomerLicenseSummaryDto;
import com.avkar.licenseportal.dto.DealerLicenseCountDto;
import com.avkar.licenseportal.dto.ExpiringLicenseFilter;
import com.avkar.licenseportal.dto.ProductLicenseCountDto;
import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.service.ReportService;
import com.avkar.licenseportal.util.ReportChartHelper;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

/**
 * Raporlama modülü (döküman §4.3 — {@code /reports/**}).
 */
@Controller
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;
    private final CurrentUserContext currentUserContext;

    public ReportController(ReportService reportService, CurrentUserContext currentUserContext) {
        this.reportService = reportService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BAYI')")
    public String index() {
        return "report/index";
    }

    @GetMapping("/product-count")
    @PreAuthorize("hasRole('ADMIN')")
    public String productCount(Model model) {
        var rows = reportService.productLicenseCountsForAdmin();
        model.addAttribute("rows", rows);
        ReportChartHelper.addBarChart(model, rows, ProductLicenseCountDto::getProductName, ProductLicenseCountDto::getLicenseCount);
        model.addAttribute("reportsIndexUrl", "/reports");
        return "report/product-count";
    }

    @GetMapping("/dealer-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAYI')")
    public String dealerCount(Model model) {
        if (currentUserContext.isAdmin()) {
            var rows = reportService.dealerLicenseCountsForAdmin();
            model.addAttribute("rows", rows);
            model.addAttribute("showDealerColumn", true);
            ReportChartHelper.addBarChart(model, rows, DealerLicenseCountDto::getDealerName, DealerLicenseCountDto::getLicenseCount);
        } else {
            var rows = reportService.dealerLicenseCountsForCurrentBayi();
            model.addAttribute("rows", rows);
            model.addAttribute("showDealerColumn", false);
            ReportChartHelper.addBarChart(model, rows, DealerLicenseCountDto::getDealerName, DealerLicenseCountDto::getLicenseCount);
        }
        model.addAttribute("reportsIndexUrl", "/reports");
        return "report/dealer-count";
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAYI')")
    public String expiring(@ModelAttribute ExpiringLicenseFilter filter, Model model) {
        Page<?> page = currentUserContext.isAdmin()
                ? reportService.expiringLicensesForAdmin(filter)
                : reportService.expiringLicensesForCurrentBayi(filter);
        boolean showDealerColumn = currentUserContext.isAdmin();
        populateExpiringModel(model, filter, page, "/reports/expiring", showDealerColumn);
        model.addAttribute("reportsIndexUrl", "/reports");
        return "report/expiring";
    }

    @GetMapping("/customer-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAYI')")
    public String customerSummary(Model model) {
        if (currentUserContext.isAdmin()) {
            var rows = reportService.customerLicenseSummaryForAdmin();
            model.addAttribute("rows", rows);
            model.addAttribute("subtitle", "Tüm kurumlar — lisans adedi");
            ReportChartHelper.addBarChart(model, rows, CustomerLicenseSummaryDto::getCustomerName, CustomerLicenseSummaryDto::getLicenseCount);
        } else {
            var rows = reportService.customerLicenseSummaryForCurrentBayi();
            model.addAttribute("rows", rows);
            model.addAttribute("subtitle", "Bayinize bağlı kurumlar — tüm lisanslar");
            ReportChartHelper.addBarChart(model, rows, CustomerLicenseSummaryDto::getCustomerName, CustomerLicenseSummaryDto::getLicenseCount);
        }
        model.addAttribute("reportsIndexUrl", "/reports");
        return "report/customer-summary";
    }

    @GetMapping("/demo-distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAYI')")
    public String demoDistribution(Model model) {
        if (currentUserContext.isAdmin()) {
            model.addAttribute("distribution", reportService.demoDistributionForAdmin());
            model.addAttribute("subtitle", "Tüm lisanslar");
        } else {
            model.addAttribute("distribution", reportService.demoDistributionForCurrentBayi());
            model.addAttribute("subtitle", "Bayinize ait tüm lisanslar");
        }
        model.addAttribute("reportsIndexUrl", "/reports");
        return "report/demo-distribution";
    }

    public static void populateExpiringModel(
            Model model,
            ExpiringLicenseFilter filter,
            Page<?> page,
            String formAction,
            boolean showDealerColumn
    ) {
        model.addAttribute("filter", filter);
        model.addAttribute("licensePage", page);
        model.addAttribute("licenses", page.getContent());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("formAction", formAction);
        model.addAttribute("showDealerColumn", showDealerColumn);
        model.addAttribute("adminDetailLinks", showDealerColumn);
    }
}
