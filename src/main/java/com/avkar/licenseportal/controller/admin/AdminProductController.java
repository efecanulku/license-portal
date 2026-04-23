package com.avkar.licenseportal.controller.admin;

import com.avkar.licenseportal.dto.ProductCreateForm;
import com.avkar.licenseportal.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.listAll());
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
}

