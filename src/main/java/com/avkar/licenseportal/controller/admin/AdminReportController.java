package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.ExpiringLicenseFilter;
import com.avkar.licenseportal.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String index() {
        return "admin/reports/index";
    }

    @GetMapping("/dealer-count")
    public String dealerCount(Model model) {
        model.addAttribute("rows", reportService.dealerLicenseCountsForAdmin());
        model.addAttribute("showDealerColumn", true);
        model.addAttribute("reportsIndexUrl", "/admin/reports");
        return "reports/dealer-count";
    }

    @GetMapping("/expiring")
    public String expiring(@ModelAttribute ExpiringLicenseFilter filter, Model model) {
        Page<?> page = reportService.expiringLicensesForAdmin(filter);
        populateExpiringModel(model, filter, page, "/admin/reports/expiring", true);
        model.addAttribute("reportsIndexUrl", "/admin/reports");
        return "reports/expiring";
    }

    @GetMapping("/product-count")
    public String productCount(Model model) {
        model.addAttribute("rows", reportService.productLicenseCountsForAdmin());
        model.addAttribute("reportsIndexUrl", "/admin/reports");
        return "reports/product-count";
    }

    @GetMapping("/customer-summary")
    public String customerSummary(Model model) {
        model.addAttribute("rows", reportService.customerLicenseSummaryForAdmin());
        model.addAttribute("subtitle", "Tüm kurumlar — lisans adedi");
        model.addAttribute("reportsIndexUrl", "/admin/reports");
        return "reports/customer-summary";
    }

    @GetMapping("/demo-distribution")
    public String demoDistribution(Model model) {
        model.addAttribute("distribution", reportService.demoDistributionForAdmin());
        model.addAttribute("subtitle", "Tüm lisanslar");
        model.addAttribute("reportsIndexUrl", "/admin/reports");
        return "reports/demo-distribution";
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
