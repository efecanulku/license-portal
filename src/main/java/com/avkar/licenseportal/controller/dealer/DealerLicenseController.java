package com.avkar.licenseportal.controller.dealer;

import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.dto.LicenseListFilter;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.service.LicenseFormService;
import com.avkar.licenseportal.service.LicenseGenerationService;
import com.avkar.licenseportal.service.LicenseService;
import com.avkar.licenseportal.util.LicenseGenerationMessages;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/dealer/licenses")
@PreAuthorize("hasRole('BAYI')")
public class DealerLicenseController {
    private final LicenseFormService licenseFormService;
    private final LicenseGenerationService licenseGenerationService;
    private final LicenseService licenseService;

    public DealerLicenseController(
            LicenseFormService licenseFormService,
            LicenseGenerationService licenseGenerationService,
            LicenseService licenseService
    ) {
        this.licenseFormService = licenseFormService;
        this.licenseGenerationService = licenseGenerationService;
        this.licenseService = licenseService;
    }

    @GetMapping
    public String list(@ModelAttribute LicenseListFilter filter, Model model) {
        var options = licenseFormService.buildOptionsForCurrentBayi();
        model.addAttribute("licenses", licenseService.listForCurrentBayi(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("products", licenseService.listFilterProductsForCurrentBayi());
        model.addAttribute("customers", options.getCustomers());
        return "dealer/licenses/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateModel(model);
        return "dealer/licenses/generate";
    }

    @PostMapping("/new")
    public String generate(
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
            License license = licenseGenerationService.generateAndSave(form);
            redirectAttributes.addFlashAttribute("justGenerated", true);
            return "redirect:/dealer/licenses/" + license.getId();
        } catch (Exception e) {
            model.addAttribute("flashError", LicenseGenerationMessages.userMessage(e));
            populateModel(model);
            return "dealer/licenses/generate";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("license", licenseService.getForCurrentUser(id));
        boolean justGenerated = Boolean.TRUE.equals(model.getAttribute("justGenerated"));
        model.addAttribute("justGenerated", justGenerated);
        model.addAttribute("pageTitle", justGenerated ? "Lisans Üretildi" : "Lisans Detayı");
        model.addAttribute("heading", justGenerated ? "Lisans üretildi" : "Lisans detayı");
        model.addAttribute("backUrl", "/dealer/licenses");
        model.addAttribute("backLabel", "Lisanslarım");
        model.addAttribute("generateUrl", "/dealer/licenses/new");
        return "licenses/detail";
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
