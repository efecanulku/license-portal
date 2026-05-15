package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.LicenseGenerateForm;
import com.avkar.licenseportal.dto.LicenseListFilter;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.repository.ProductRepository;
import com.avkar.licenseportal.service.CustomerService;
import com.avkar.licenseportal.service.DealerService;
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
@RequestMapping("/admin/licenses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLicenseController {
    private final LicenseFormService licenseFormService;
    private final LicenseGenerationService licenseGenerationService;
    private final LicenseService licenseService;
    private final DealerService dealerService;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    public AdminLicenseController(
            LicenseFormService licenseFormService,
            LicenseGenerationService licenseGenerationService,
            LicenseService licenseService,
            DealerService dealerService,
            ProductRepository productRepository,
            CustomerService customerService
    ) {
        this.licenseFormService = licenseFormService;
        this.licenseGenerationService = licenseGenerationService;
        this.licenseService = licenseService;
        this.dealerService = dealerService;
        this.productRepository = productRepository;
        this.customerService = customerService;
    }

    @GetMapping
    public String list(@ModelAttribute LicenseListFilter filter, Model model) {
        var licensePage = licenseService.listForAdmin(filter);
        model.addAttribute("licensePage", licensePage);
        model.addAttribute("licenses", licensePage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("dealers", dealerService.listAll());
        model.addAttribute("products", productRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("customers", customerService.listForAdmin(filter.getDealerId()));
        return "admin/licenses/list";
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
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin/licenses/generate";
        }

        try {
            License license = licenseGenerationService.generateAndSave(form);
            redirectAttributes.addFlashAttribute("justGenerated", true);
            return "redirect:/admin/licenses/" + license.getId();
        } catch (Exception e) {
            model.addAttribute("flashError", LicenseGenerationMessages.userMessage(e));
            populateModel(model);
            return "admin/licenses/generate";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("license", licenseService.getForCurrentUser(id));
        boolean justGenerated = Boolean.TRUE.equals(model.getAttribute("justGenerated"));
        model.addAttribute("justGenerated", justGenerated);
        model.addAttribute("pageTitle", justGenerated ? "Lisans Üretildi" : "Lisans Detayı");
        model.addAttribute("heading", justGenerated ? "Lisans üretildi" : "Lisans detayı");
        model.addAttribute("backUrl", "/admin/licenses");
        model.addAttribute("backLabel", "Lisans listesi");
        model.addAttribute("generateUrl", "/admin/licenses/new");
        return "licenses/detail";
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
