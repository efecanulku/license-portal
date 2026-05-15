package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.dto.DealerLicenseCountDto;
import com.avkar.licenseportal.entity.License;
import com.avkar.licenseportal.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query("""
            SELECT l FROM License l
            JOIN FETCH l.product p
            JOIN FETCH l.customer c
            JOIN FETCH l.dealer d
            JOIN FETCH l.createdBy
            WHERE (:dealerId IS NULL OR d.id = :dealerId)
              AND (:productId IS NULL OR p.id = :productId)
              AND (:customerId IS NULL OR c.id = :customerId)
              AND (:validUntilFrom IS NULL OR l.validUntil >= :validUntilFrom)
              AND (:validUntilTo IS NULL OR l.validUntil <= :validUntilTo)
            ORDER BY l.createdAt DESC
            """)
    List<License> searchWithFilters(
            @Param("dealerId") Long dealerId,
            @Param("productId") Long productId,
            @Param("customerId") Long customerId,
            @Param("validUntilFrom") LocalDate validUntilFrom,
            @Param("validUntilTo") LocalDate validUntilTo
    );

    /**
     * BAYI: yalnızca oturumdaki kullanıcının ürettiği lisanslar (döküman §4.2.2).
     */
    @Query("""
            SELECT l FROM License l
            JOIN FETCH l.product p
            JOIN FETCH l.customer c
            JOIN FETCH l.dealer d
            JOIN FETCH l.createdBy u
            WHERE u.id = :userId
              AND d.id = :dealerId
              AND (:productId IS NULL OR p.id = :productId)
              AND (:customerId IS NULL OR c.id = :customerId)
              AND (:validUntilFrom IS NULL OR l.validUntil >= :validUntilFrom)
              AND (:validUntilTo IS NULL OR l.validUntil <= :validUntilTo)
            ORDER BY l.createdAt DESC
            """)
    List<License> searchForBayiUser(
            @Param("userId") Long userId,
            @Param("dealerId") Long dealerId,
            @Param("productId") Long productId,
            @Param("customerId") Long customerId,
            @Param("validUntilFrom") LocalDate validUntilFrom,
            @Param("validUntilTo") LocalDate validUntilTo
    );

    @Query("""
            SELECT DISTINCT p FROM License l
            JOIN l.product p
            WHERE l.createdBy.id = :userId
            ORDER BY p.name ASC
            """)
    List<Product> findDistinctProductsByCreatedByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT new com.avkar.licenseportal.dto.DealerLicenseCountDto(
                d.id, d.name, COUNT(l)
            )
            FROM License l
            JOIN l.dealer d
            GROUP BY d.id, d.name
            ORDER BY COUNT(l) DESC, d.name ASC
            """)
    List<DealerLicenseCountDto> countLicensesByDealerForAdmin();

    @Query("""
            SELECT new com.avkar.licenseportal.dto.DealerLicenseCountDto(
                d.id, d.name, COUNT(l)
            )
            FROM License l
            JOIN l.dealer d
            JOIN l.createdBy u
            WHERE u.id = :userId AND d.id = :dealerId
            GROUP BY d.id, d.name
            """)
    List<DealerLicenseCountDto> countLicensesByDealerForBayiUser(
            @Param("userId") Long userId,
            @Param("dealerId") Long dealerId
    );

    @Query(
            value = """
                    SELECT l FROM License l
                    JOIN FETCH l.product
                    JOIN FETCH l.customer
                    JOIN FETCH l.dealer
                    JOIN FETCH l.createdBy
                    WHERE (:userId IS NULL OR l.createdBy.id = :userId)
                      AND (:dealerId IS NULL OR l.dealer.id = :dealerId)
                      AND (
                           (:status = 'ALL' AND l.validUntil <= :expiringUntil)
                        OR (:status = 'EXPIRED' AND l.validUntil < :today)
                        OR (:status = 'EXPIRING' AND l.validUntil >= :today AND l.validUntil <= :expiringUntil)
                      )
                    ORDER BY l.validUntil ASC, l.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(l) FROM License l
                    WHERE (:userId IS NULL OR l.createdBy.id = :userId)
                      AND (:dealerId IS NULL OR l.dealer.id = :dealerId)
                      AND (
                           (:status = 'ALL' AND l.validUntil <= :expiringUntil)
                        OR (:status = 'EXPIRED' AND l.validUntil < :today)
                        OR (:status = 'EXPIRING' AND l.validUntil >= :today AND l.validUntil <= :expiringUntil)
                      )
                    """
    )
    Page<License> searchExpiringLicenses(
            @Param("userId") Long userId,
            @Param("dealerId") Long dealerId,
            @Param("today") LocalDate today,
            @Param("expiringUntil") LocalDate expiringUntil,
            @Param("status") String status,
            Pageable pageable
    );
}
