package com.avkar.licenseportal.controller.dealer;

import com.avkar.licenseportal.controller.HomeController;
import com.avkar.licenseportal.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Faz 4 — Bayi dashboard ({@code /dealer/**}).
 */
@Controller
@RequestMapping("/dealer")
@PreAuthorize("hasRole('BAYI')")
public class DealerDashboardController {
    private final ReportService reportService;

    public DealerDashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("expiringSoonCount",
                reportService.countExpiringSoonForCurrentBayi(HomeController.EXPIRING_SOON_DAYS));
        model.addAttribute("expiringReportUrl", "/reports/expiring");
        model.addAttribute("expiringSoonDays", HomeController.EXPIRING_SOON_DAYS);
        return "dealer/dashboard";
    }
}
