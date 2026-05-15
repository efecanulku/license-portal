package com.avkar.licenseportal.security;

import com.avkar.licenseportal.entity.Role;
import com.avkar.licenseportal.entity.User;
import com.avkar.licenseportal.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Oturumdaki kullanıcı ve bayi bilgisinin tek kaynağı (döküman: SecurityUtils / session).
 */
@Service
public class CurrentUserContext {
    private final UserRepository userRepository;

    public CurrentUserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameWithDealer(username);
    }

    @Transactional(readOnly = true)
    public User requireUser() {
        return findCurrentUser().orElseThrow(() -> new IllegalStateException("Oturum bulunamadı."));
    }

    @Transactional(readOnly = true)
    public boolean isAdmin() {
        return findCurrentUser().map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isBayi() {
        return findCurrentUser().map(u -> u.getRole() == Role.BAYI).orElse(false);
    }

    /**
     * BAYI rolü için oturumdaki bayinin id'si. Admin çağırırsa hata.
     */
    @Transactional(readOnly = true)
    public Long requireDealerId() {
        User user = requireUser();
        if (user.getRole() != Role.BAYI || user.getDealer() == null) {
            throw new IllegalStateException("Bayi bilgisi bulunamadı.");
        }
        return user.getDealer().getId();
    }
}
