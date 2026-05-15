package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.Role;
import com.avkar.licenseportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.dealer WHERE u.username = :username")
    Optional<User> findByUsernameWithDealer(@Param("username") String username);

    List<User> findByDealer_IdAndRoleOrderByUsernameAsc(Long dealerId, Role role);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);
}

