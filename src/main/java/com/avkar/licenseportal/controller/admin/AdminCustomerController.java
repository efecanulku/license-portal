package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.CustomerForm;
import com.avkar.licenseportal.service.CustomerService;
import com.avkar.licenseportal.service.DealerService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {
    private final CustomerService customerService;
    private final DealerService dealerService;

    public AdminCustomerController(CustomerService customerService, DealerService dealerService) {
        this.customerService = customerService;
        this.dealerService = dealerService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long dealerId, Model model) {
        model.addAttribute("customers", customerService.listForAdmin(dealerId));
        model.addAttribute("dealers", dealerService.listAll());
        model.addAttribute("selectedDealerId", dealerId);
        return "admin/customers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dealers", dealerService.listAll());
        model.addAttribute("showDealerSelect", true);
        model.addAttribute("cancelUrl", "/admin/customers");
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CustomerForm());
        }
        return "admin/customers/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") CustomerForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/customers/new";
        }

        customerService.createForAdmin(form);
        redirectAttributes.addFlashAttribute("flashSuccess", "Kurum oluşturuldu.");
        return "redirect:/admin/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var customer = customerService.getForAdmin(id);
            model.addAttribute("showDealerSelect", false);
            model.addAttribute("createdByDealerName",
                    customer.getCreatedByDealer() != null ? customer.getCreatedByDealer().getName() : "—");
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", toForm(customer));
            }
            model.addAttribute("customerId", id);
            model.addAttribute("cancelUrl", "/admin/customers");
            return "admin/customers/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kurum bulunamadı.");
            return "redirect:/admin/customers";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") CustomerForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/customers/" + id + "/edit";
        }

        try {
            customerService.updateForAdmin(id, form);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kurum bulunamadı.");
            return "redirect:/admin/customers";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Kurum güncellendi.");
        return "redirect:/admin/customers";
    }

    private static CustomerForm toForm(com.avkar.licenseportal.entity.Customer customer) {
        CustomerForm form = new CustomerForm();
        form.setName(customer.getName());
        form.setTaxNumber(customer.getTaxNumber());
        form.setAddress(customer.getAddress());
        form.setContactName(customer.getContactName());
        form.setContactEmail(customer.getContactEmail());
        form.setContactPhone(customer.getContactPhone());
        if (customer.getCreatedByDealer() != null) {
            form.setCreatedByDealerId(customer.getCreatedByDealer().getId());
        }
        return form;
    }
}
