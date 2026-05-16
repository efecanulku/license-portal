package com.avkar.licenseportal.controller.report;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Eski {@code /admin/reports/**} ve {@code /dealer/reports/**} yollarını dökümandaki {@code /reports/**} adresine yönlendirir.
 */
@Controller
public class LegacyReportRedirectController {

    @GetMapping({"/admin/reports", "/admin/reports/"})
    public String redirectAdminRoot() {
        return "redirect:/reports";
    }

    @GetMapping("/admin/reports/**")
    public String redirectAdminReports(HttpServletRequest request) {
        return buildRedirect(request, "/admin/reports");
    }

    @GetMapping({"/dealer/reports", "/dealer/reports/"})
    public String redirectDealerRoot() {
        return "redirect:/reports";
    }

    @GetMapping("/dealer/reports/**")
    public String redirectDealerReports(HttpServletRequest request) {
        return buildRedirect(request, "/dealer/reports");
    }

    private static String buildRedirect(HttpServletRequest request, String prefix) {
        String uri = request.getRequestURI();
        String path = uri.length() > prefix.length() ? uri.substring(prefix.length()) : "";
        String qs = request.getQueryString();
        return "redirect:/reports" + path + (qs != null ? "?" + qs : "");
    }
}
