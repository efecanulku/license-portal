package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.DealerForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/dealers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDealerController {
    private final DealerService dealerService;

    public AdminDealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("dealers", dealerService.listAll());
        return "admin/dealers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new DealerForm());
        }
        return "admin/dealers/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") DealerForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/new";
        }

        dealerService.create(form);
        redirectAttributes.addFlashAttribute("flashSuccess", "Bayi oluşturuldu.");
        return "redirect:/admin/dealers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var d = dealerService.getById(id);
            if (!model.containsAttribute("form")) {
                DealerForm form = new DealerForm();
                form.setName(d.getName());
                form.setContactName(d.getContactName());
                form.setEmail(d.getEmail());
                form.setPhone(d.getPhone());
                form.setActive(Boolean.TRUE.equals(d.getActive()));
                model.addAttribute("form", form);
            }
            model.addAttribute("dealerId", id);
            return "admin/dealers/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") DealerForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/dealers/" + id + "/edit";
        }

        try {
            dealerService.update(id, form);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Bayi bulunamadı.");
            return "redirect:/admin/dealers";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Bayi güncellendi.");
        return "redirect:/admin/dealers";
    }
}

