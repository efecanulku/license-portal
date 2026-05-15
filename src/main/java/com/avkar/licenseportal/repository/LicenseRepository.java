package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    @Query("""
            SELECT l FROM License l
            JOIN FETCH l.product
            JOIN FETCH l.customer
            JOIN FETCH l.dealer
            JOIN FETCH l.createdBy
            WHERE l.id = :id
            """)
    Optional<License> findByIdWithDetails(@Param("id") Long id);
}
