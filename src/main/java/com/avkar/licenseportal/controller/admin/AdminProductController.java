package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.ProductCreateForm;
import com.avkar.licenseportal.dto.ProductUpdateForm;
import com.avkar.licenseportal.dto.SimplePageParams;
import com.avkar.licenseportal.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(@ModelAttribute SimplePageParams pageParams, Model model) {
        var itemPage = productService.listPage(pageParams);
        model.addAttribute("itemPage", itemPage);
        model.addAttribute("products", itemPage.getContent());
        model.addAttribute("pageParams", pageParams);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProductCreateForm());
        }
        return "admin/products/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") ProductCreateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/new";
        }

        try {
            productService.create(form);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün kodu zaten kullanılıyor.");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/new";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Ürün oluşturuldu.");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var p = productService.getById(id);
            if (!model.containsAttribute("form")) {
                ProductUpdateForm form = new ProductUpdateForm();
                form.setName(p.getName());
                form.setCode(p.getCode());
                form.setDescription(p.getDescription());
                model.addAttribute("form", form);
            }
            model.addAttribute("productId", id);
            return "admin/products/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün bulunamadı.");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ProductUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/" + id + "/edit";
        }

        try {
            productService.update(id, form);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/" + id + "/edit";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün kodu zaten kullanılıyor.");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/" + id + "/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün bulunamadı.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Ürün güncellendi.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleActive(id);
            redirectAttributes.addFlashAttribute("flashSuccess", "Ürün durumu güncellendi.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("flashError", "Ürün bulunamadı.");
        }
        return "redirect:/admin/products";
    }
}

