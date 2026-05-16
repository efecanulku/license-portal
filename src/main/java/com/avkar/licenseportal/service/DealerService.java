package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.DealerForm;
import com.avkar.licenseportal.dto.SimplePageParams;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.repository.DealerRepository;
import com.avkar.licenseportal.security.DealerAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DealerService {
    private final DealerRepository dealerRepository;
    private final DealerAccessGuard dealerAccessGuard;

    public DealerService(DealerRepository dealerRepository, DealerAccessGuard dealerAccessGuard) {
        this.dealerRepository = dealerRepository;
        this.dealerAccessGuard = dealerAccessGuard;
    }

    public List<Dealer> listAll() {
        dealerAccessGuard.requireAdmin();
        return dealerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Dealer> listPage(SimplePageParams params) {
        dealerAccessGuard.requireAdmin();
        PageRequest pageable = PageRequest.of(
                params.getPage(),
                SimplePageParams.PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name")
        );
        String q = params.normalizedQuery();
        if (q == null) {
            return dealerRepository.findAllByOrderByNameAsc(pageable);
        }
        return dealerRepository.searchPage(q, pageable);
    }

    public Dealer getById(Long id) {
        return dealerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Dealer not found: " + id));
    }

    @Transactional
    public Dealer create(DealerForm form) {
        dealerAccessGuard.requireAdmin();
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
        dealerAccessGuard.requireAdmin();
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

