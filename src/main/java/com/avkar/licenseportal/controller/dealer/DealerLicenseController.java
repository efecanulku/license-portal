package com.avkar.licenseportal.controller.dealer;

import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.service.LicenseFormService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/dealer/licenses")
@PreAuthorize("hasRole('BAYI')")
public class DealerLicenseController {
    private final LicenseFormService licenseFormService;

    public DealerLicenseController(LicenseFormService licenseFormService) {
        this.licenseFormService = licenseFormService;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateModel(model);
        return "dealer/licenses/generate";
    }

    @PostMapping("/new")
    public String validateForm(
            @Valid @ModelAttribute("form") LicenseGenerateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "dealer/licenses/generate";
        }

        try {
            licenseFormService.validateFormSelections(form);
        } catch (Exception e) {
            model.addAttribute("flashError", "Seçimler geçersiz: " + e.getMessage());
            populateModel(model);
            return "dealer/licenses/generate";
        }

        redirectAttributes.addFlashAttribute("flashSuccess",
                "Form doğrulandı. Lisans üretimi Gün 16'da eklenecek.");
        return "redirect:/dealer/licenses/new";
    }

    private void populateModel(Model model) {
        var options = licenseFormService.buildOptionsForCurrentBayi();
        model.addAttribute("products", options.getProducts());
        model.addAttribute("customers", options.getCustomers());
        model.addAttribute("restrictedProducts", true);
        if (!model.containsAttribute("form")) {
            LicenseGenerateForm form = new LicenseGenerateForm();
            form.setValidUntil(LocalDate.now().plusYears(1));
            model.addAttribute("form", form);
        }
    }
}
