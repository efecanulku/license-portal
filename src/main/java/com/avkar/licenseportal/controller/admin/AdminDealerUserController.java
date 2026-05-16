package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.DealerUserCreateForm;
import com.avkar.licenseportal.dto.DealerUserUpdateForm;
import com.avkar.licenseportal.dto.SimplePageParams;
import com.avkar.licenseportal.service.DealerService;
import com.avkar.licenseportal.service.DealerUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/dealers/{dealerId}/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDealerUserController {
    private final DealerService dealerService;
    private final DealerUserService dealerUserService;

    public AdminDealerUserController(DealerService dealerService, DealerUserService dealerUserService) {
        this.dealerService = dealerService;
        this.dealerUserService = dealerUserService;
    }

    @GetMapping
    public String list(
            @PathVariable Long dealerId,
            @ModelAttribute SimplePageParams pageParams,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var dealer = dealerService.getById(dealerId);
            var itemPage = dealerUserService.listPage(dealerId, pageParams);
            model.addAttribute("dealer", dealer);
            model.addAttribute("dealerId", dealerId);
            model.addAttribute("itemPage", itemPage);
            model.addAttribute("users", itemPage.getContent());
            model.addAttribute("pageParams", pageParams);
            model.addAttribute("listFormAction", "/admin/dealers/" + dealerId + "/users");
            return "admin/dealer-users/list";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        }
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long dealerId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("dealer", dealerService.getById(dealerId));
            model.addAttribute("dealerId", dealerId);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", new DealerUserCreateForm());
            }
            return "admin/dealer-users/new";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        }
    }

    @PostMapping
    public String create(
            @PathVariable Long dealerId,
            @Valid @ModelAttribute("form") DealerUserCreateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/" + dealerId + "/users/new";
        }

        try {
            dealerUserService.create(dealerId, form);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kullanıcı adı zaten kullanılıyor.");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/" + dealerId + "/users/new";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Bayi kullanıcısı oluşturuldu.");
        return "redirect:/admin/dealers/" + dealerId + "/users";
    }

    @GetMapping("/{userId}/edit")
    public String editForm(
            @PathVariable Long dealerId,
            @PathVariable Long userId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var user = dealerUserService.getBayiUser(dealerId, userId);
            model.addAttribute("dealer", dealerService.getById(dealerId));
            model.addAttribute("dealerId", dealerId);
            model.addAttribute("userId", userId);
            if (!model.containsAttribute("form")) {
                DealerUserUpdateForm form = new DealerUserUpdateForm();
                form.setUsername(user.getUsername());
                form.setFullName(user.getFullName());
                form.setEmail(user.getEmail());
                form.setActive(Boolean.TRUE.equals(user.getActive()));
                model.addAttribute("form", form);
            }
            return "admin/dealer-users/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kullanıcı bulunamadı.");
            return "redirect:/admin/dealers/" + dealerId + "/users";
        }
    }

    @PostMapping("/{userId}")
    public String update(
            @PathVariable Long dealerId,
            @PathVariable Long userId,
            @Valid @ModelAttribute("form") DealerUserUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/" + dealerId + "/users/" + userId + "/edit";
        }

        try {
            dealerUserService.update(dealerId, userId, form);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kullanıcı bulunamadı.");
            return "redirect:/admin/dealers/" + dealerId + "/users";
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("Password")
                    ? "Şifre en az 8 karakter olmalı."
                    : "Kullanıcı adı zaten kullanılıyor.";
            redirectAttributes.addFlashAttribute("flashError", msg);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/" + dealerId + "/users/" + userId + "/edit";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Kullanıcı güncellendi.");
        return "redirect:/admin/dealers/" + dealerId + "/users";
    }

    @PostMapping("/{userId}/toggle-active")
    public String toggleActive(
            @PathVariable Long dealerId,
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealerUserService.toggleActive(dealerId, userId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Kullanıcı durumu güncellendi.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kullanıcı bulunamadı.");
        }
        return "redirect:/admin/dealers/" + dealerId + "/users";
    }
}
