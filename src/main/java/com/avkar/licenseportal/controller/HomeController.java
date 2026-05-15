package com.avkar.licenseportal.controller;

import com.avkar.licenseportal.security.CurrentUserContext;
import com.avkar.licenseportal.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    public static final int EXPIRING_SOON_DAYS = 30;

    private final ReportService reportService;
    private final CurrentUserContext currentUserContext;

    public HomeController(ReportService reportService, CurrentUserContext currentUserContext) {
        this.reportService = reportService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/")
    public String home(Model model) {
        if (currentUserContext.isAdmin()) {
            model.addAttribute("expiringSoonCount", reportService.countExpiringSoonForAdmin(EXPIRING_SOON_DAYS));
            model.addAttribute("expiringReportUrl", "/admin/reports/expiring");
        } else if (currentUserContext.isBayi()) {
            model.addAttribute("expiringSoonCount", reportService.countExpiringSoonForCurrentBayi(EXPIRING_SOON_DAYS));
            model.addAttribute("expiringReportUrl", "/dealer/reports/expiring");
        }
        model.addAttribute("expiringSoonDays", EXPIRING_SOON_DAYS);
        return "home";
    }
}
