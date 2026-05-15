package com.avkar.licenseportal.controller.dealer;

import com.avkar.licenseportal.controller.admin.AdminReportController;
import com.avkar.licenseportal.dto.ExpiringLicenseFilter;
import com.avkar.licenseportal.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dealer/reports")
@PreAuthorize("hasRole('BAYI')")
public class DealerReportController {
    private final ReportService reportService;

    public DealerReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String index() {
        return "dealer/reports/index";
    }

    @GetMapping("/dealer-count")
    public String dealerCount(Model model) {
        model.addAttribute("rows", reportService.dealerLicenseCountsForCurrentBayi());
        model.addAttribute("showDealerColumn", false);
        model.addAttribute("reportsIndexUrl", "/dealer/reports");
        return "reports/dealer-count";
    }

    @GetMapping("/expiring")
    public String expiring(@ModelAttribute ExpiringLicenseFilter filter, Model model) {
        Page<?> page = reportService.expiringLicensesForCurrentBayi(filter);
        AdminReportController.populateExpiringModel(
                model, filter, page, "/dealer/reports/expiring", false
        );
        model.addAttribute("reportsIndexUrl", "/dealer/reports");
        return "reports/expiring";
    }

    @GetMapping("/customer-summary")
    public String customerSummary(Model model) {
        model.addAttribute("rows", reportService.customerLicenseSummaryForCurrentBayi());
        model.addAttribute("subtitle", "Kendi kurumlarınız — sizin ürettiğiniz lisanslar");
        model.addAttribute("reportsIndexUrl", "/dealer/reports");
        return "reports/customer-summary";
    }

    @GetMapping("/demo-distribution")
    public String demoDistribution(Model model) {
        model.addAttribute("distribution", reportService.demoDistributionForCurrentBayi());
        model.addAttribute("subtitle", "Sizin ürettiğiniz lisanslar");
        model.addAttribute("reportsIndexUrl", "/dealer/reports");
        return "reports/demo-distribution";
    }
}
