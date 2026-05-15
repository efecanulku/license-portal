package com.avkar.licenseportal.controller;

import com.avkar.licenseportal.dto.PasswordChangeForm;
import com.avkar.licenseportal.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PasswordChangeForm());
        }
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("form") PasswordChangeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "account/password";
        }

        try {
            accountService.changePassword(form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Şifreniz güncellendi.");
            return "redirect:/account/password";
        } catch (IllegalArgumentException e) {
            model.addAttribute("flashError", e.getMessage());
            return "account/password";
        }
    }
}
