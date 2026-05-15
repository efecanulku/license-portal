package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.DealerUserCreateForm;
import com.avkar.licenseportal.dto.DealerUserUpdateForm;
import com.avkar.licenseportal.entity.Dealer;
import com.avkar.licenseportal.entity.Role;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DealerUserService {
    private final UserRepository userRepository;
    private final DealerService dealerService;
    private final PasswordEncoder passwordEncoder;

    public DealerUserService(
            UserRepository userRepository,
            DealerService dealerService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.dealerService = dealerService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> listBayiUsers(Long dealerId) {
        ensureDealerExists(dealerId);
        return userRepository.findByDealer_IdAndRoleOrderByUsernameAsc(dealerId, Role.BAYI);
    }

    @Transactional(readOnly = true)
    public User getBayiUser(Long dealerId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        if (user.getRole() != Role.BAYI || user.getDealer() == null || !dealerId.equals(user.getDealer().getId())) {
            throw new NoSuchElementException("Bayi user not found for dealer: " + dealerId);
        }
        return user;
    }

    @Transactional
    public User create(Long dealerId, DealerUserCreateForm form) {
        Dealer dealer = dealerService.getById(dealerId);
        String username = form.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setFullName(emptyToNull(form.getFullName()));
        user.setEmail(emptyToNull(form.getEmail()));
        user.setRole(Role.BAYI);
        user.setDealer(dealer);
        user.setActive(form.isActive());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long dealerId, Long userId, DealerUserUpdateForm form) {
        User user = getBayiUser(dealerId, userId);
        String username = form.getUsername().trim();
        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        user.setUsername(username);
        user.setFullName(emptyToNull(form.getFullName()));
        user.setEmail(emptyToNull(form.getEmail()));
        user.setActive(form.isActive());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            if (form.getPassword().length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void toggleActive(Long dealerId, Long userId) {
        User user = getBayiUser(dealerId, userId);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void ensureDealerExists(Long dealerId) {
        dealerService.getById(dealerId);
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
