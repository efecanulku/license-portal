package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.service.DealerProductPermissionService;
import com.avkar.licenseportal.service.DealerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/dealers/{dealerId}/product-permissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDealerProductPermissionController {
    private final DealerService dealerService;
    private final DealerProductPermissionService permissionService;

    public AdminDealerProductPermissionController(
            DealerService dealerService,
            DealerProductPermissionService permissionService
    ) {
        this.dealerService = dealerService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(@PathVariable Long dealerId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("dealer", dealerService.getById(dealerId));
            model.addAttribute("dealerId", dealerId);
            model.addAttribute("permissions", permissionService.listForDealer(dealerId));
            model.addAttribute("grantableProducts", permissionService.listGrantableProducts(dealerId));
            return "admin/dealer-product-permissions/list";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        }
    }

    @PostMapping("/grant")
    public String grant(
            @PathVariable Long dealerId,
            @RequestParam(required = false) Long productId,
            RedirectAttributes redirectAttributes
    ) {
        if (productId == null) {
            redirectAttributes.addFlashAttribute("flashError", "Lütfen bir ürün seçin.");
            return "redirect:/admin/dealers/" + dealerId + "/product-permissions";
        }

        try {
            permissionService.grant(dealerId, productId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Ürün yetkisi verildi.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün bulunamadı.");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("Inactive")
                    ? "Pasif ürüne yetki verilemez."
                    : "Bu ürün yetkisi zaten tanımlı.";
            redirectAttributes.addFlashAttribute("flashError", msg);
        }

        return "redirect:/admin/dealers/" + dealerId + "/product-permissions";
    }

    @PostMapping("/revoke")
    public String revoke(
            @PathVariable Long dealerId,
            @RequestParam(required = false) Long productId,
            RedirectAttributes redirectAttributes
    ) {
        if (productId == null) {
            redirectAttributes.addFlashAttribute("flashError", "Geçersiz istek.");
            return "redirect:/admin/dealers/" + dealerId + "/product-permissions";
        }

        try {
            permissionService.revoke(dealerId, productId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Ürün yetkisi kaldırıldı.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Yetki bulunamadı.");
        }

        return "redirect:/admin/dealers/" + dealerId + "/product-permissions";
    }
}
