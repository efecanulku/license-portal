package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.service.LicenseFormService;
import com.avkar.licenseportal.service.LicenseGenerationService;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/licenses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLicenseController {
    private final LicenseFormService licenseFormService;
    private final LicenseGenerationService licenseGenerationService;

    public AdminLicenseController(
            LicenseFormService licenseFormService,
            LicenseGenerationService licenseGenerationService
    ) {
        this.licenseFormService = licenseFormService;
        this.licenseGenerationService = licenseGenerationService;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateModel(model);
        return "admin/licenses/generate";
    }

    @PostMapping("/new")
    public String generate(
            @Valid @ModelAttribute("form") LicenseGenerateForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin/licenses/generate";
        }

        try {
            License license = licenseGenerationService.generateAndSave(form);
            return "redirect:/admin/licenses/" + license.getId();
        } catch (Exception e) {
            model.addAttribute("flashError", "Lisans üretilemedi: " + e.getMessage());
            populateModel(model);
            return "admin/licenses/generate";
        }
    }

    @GetMapping("/{id}")
    public String result(@PathVariable Long id, Model model) {
        License license = licenseGenerationService.getForCurrentUser(id);
        model.addAttribute("license", license);
        model.addAttribute("backUrl", "/admin/licenses/new");
        return "licenses/result";
    }

    private void populateModel(Model model) {
        var options = licenseFormService.buildOptionsForAdmin();
        model.addAttribute("products", options.getProducts());
        model.addAttribute("customers", options.getCustomers());
        if (!model.containsAttribute("form")) {
            LicenseGenerateForm form = new LicenseGenerateForm();
            form.setValidUntil(LocalDate.now().plusYears(1));
            model.addAttribute("form", form);
        }
    }
}
