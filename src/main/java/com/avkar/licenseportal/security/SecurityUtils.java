package com.avkar.licenseportal.security;

import com.avkar.licenseportal.entity.Role;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser(UserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    public static Long requireCurrentDealerId(UserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Oturum bulunamadı.");
        }
        User user = userRepository.findByUsernameWithDealer(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı."));
        if (user.getRole() != Role.BAYI || user.getDealer() == null) {
            throw new IllegalStateException("Bayi bilgisi bulunamadı.");
        }
        return user.getDealer().getId();
    }
}
