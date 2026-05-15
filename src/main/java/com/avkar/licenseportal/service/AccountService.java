package com.avkar.licenseportal.service;

import com.avkar.licenseportal.dto.PasswordChangeForm;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.UserRepository;
import com.avkar.licenseportal.security.CurrentUserContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            UserRepository userRepository,
            CurrentUserContext currentUserContext,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.currentUserContext = currentUserContext;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(PasswordChangeForm form) {
        User user = currentUserContext.requireUser();
        User managed = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı."));

        if (!passwordEncoder.matches(form.getCurrentPassword(), managed.getPasswordHash())) {
            throw new IllegalArgumentException("Mevcut şifre hatalı.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Yeni şifre ve tekrarı eşleşmiyor.");
        }
        if (passwordEncoder.matches(form.getNewPassword(), managed.getPasswordHash())) {
            throw new IllegalArgumentException("Yeni şifre mevcut şifreden farklı olmalıdır.");
        }

        managed.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(managed);
    }
}
