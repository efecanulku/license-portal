package com.avkar.licenseportal.controller.dealer;

import com.avkar.licenseportal.dto.CustomerForm;
import com.avkar.licenseportal.entity.Customer;
import com.avkar.licenseportal.service.CustomerService;
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
@RequestMapping("/dealer/customers")
@PreAuthorize("hasRole('BAYI')")
public class DealerCustomerController {
    private final CustomerService customerService;

    public DealerCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.listForCurrentBayi());
        return "dealer/customers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("cancelUrl", "/dealer/customers");
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CustomerForm());
        }
        return "dealer/customers/new";
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
            return "redirect:/dealer/customers/new";
        }

        customerService.createForCurrentBayi(form);
        redirectAttributes.addFlashAttribute("flashSuccess", "Kurum oluşturuldu.");
        return "redirect:/dealer/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.getForCurrentBayi(id);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", toForm(customer));
            }
            model.addAttribute("customerId", id);
            model.addAttribute("cancelUrl", "/dealer/customers");
            return "dealer/customers/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kurum bulunamadı.");
            return "redirect:/dealer/customers";
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
            return "redirect:/dealer/customers/" + id + "/edit";
        }

        try {
            customerService.updateForCurrentBayi(id, form);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Kurum bulunamadı.");
            return "redirect:/dealer/customers";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Kurum güncellendi.");
        return "redirect:/dealer/customers";
    }

    private static CustomerForm toForm(Customer customer) {
        CustomerForm form = new CustomerForm();
        form.setName(customer.getName());
        form.setTaxNumber(customer.getTaxNumber());
        form.setAddress(customer.getAddress());
        form.setContactName(customer.getContactName());
        form.setContactEmail(customer.getContactEmail());
        form.setContactPhone(customer.getContactPhone());
        return form;
    }
}
