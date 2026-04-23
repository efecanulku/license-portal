package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.DealerForm;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.repository.DealerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DealerService {
    private final DealerRepository dealerRepository;

    public DealerService(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    public List<Dealer> listAll() {
        return dealerRepository.findAll();
    }

    public Dealer getById(Long id) {
        return dealerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Dealer not found: " + id));
    }

    @Transactional
    public Dealer create(DealerForm form) {
        Dealer d = new Dealer();
        d.setName(form.getName().trim());
        d.setContactName(emptyToNull(form.getContactName()));
        d.setEmail(emptyToNull(form.getEmail()));
        d.setPhone(emptyToNull(form.getPhone()));
        d.setActive(form.isActive());
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        return dealerRepository.save(d);
    }

    @Transactional
    public Dealer update(Long id, DealerForm form) {
        Dealer d = getById(id);
        d.setName(form.getName().trim());
        d.setContactName(emptyToNull(form.getContactName()));
        d.setEmail(emptyToNull(form.getEmail()));
        d.setPhone(emptyToNull(form.getPhone()));
        d.setActive(form.isActive());
        d.setUpdatedAt(LocalDateTime.now());
        return dealerRepository.save(d);
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

