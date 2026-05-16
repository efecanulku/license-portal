package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("""
            SELECT DISTINCT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            LEFT JOIN c.linkedDealers ld
            WHERE (:dealerId IS NULL OR ld.id = :dealerId)
              AND (:q IS NULL OR :q = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY c.name ASC
            """)
    List<Customer> findAllWithFilters(@Param("dealerId") Long dealerId, @Param("q") String q);

    @Query(
            value = """
                    SELECT DISTINCT c FROM Customer c
                    LEFT JOIN FETCH c.createdByDealer
                    LEFT JOIN c.linkedDealers ld
                    WHERE (:dealerId IS NULL OR ld.id = :dealerId)
                      AND (:q IS NULL OR :q = ''
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    ORDER BY c.name ASC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT c) FROM Customer c
                    LEFT JOIN c.linkedDealers ld
                    WHERE (:dealerId IS NULL OR ld.id = :dealerId)
                      AND (:q IS NULL OR :q = ''
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    """
    )
    Page<Customer> findAllWithFiltersPage(
            @Param("dealerId") Long dealerId,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            JOIN c.linkedDealers ld
            WHERE ld.id = :dealerId
              AND (:q IS NULL OR :q = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY c.name ASC
            """)
    List<Customer> findByLinkedDealerId(@Param("dealerId") Long dealerId, @Param("q") String q);

    @Query(
            value = """
                    SELECT DISTINCT c FROM Customer c
                    JOIN c.linkedDealers ld
                    WHERE ld.id = :dealerId
                      AND (:q IS NULL OR :q = ''
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    ORDER BY c.name ASC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT c) FROM Customer c
                    JOIN c.linkedDealers ld
                    WHERE ld.id = :dealerId
                      AND (:q IS NULL OR :q = ''
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.taxNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    """
    )
    Page<Customer> findByLinkedDealerIdPage(
            @Param("dealerId") Long dealerId,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            LEFT JOIN FETCH c.linkedDealers
            WHERE c.id = :id
            """)
    Optional<Customer> findByIdWithDealer(@Param("id") Long id);

    @Query("""
            SELECT c FROM Customer c
            JOIN c.linkedDealers ld
            WHERE c.id = :id AND ld.id = :dealerId
            """)
    Optional<Customer> findByIdAndLinkedDealerId(@Param("id") Long id, @Param("dealerId") Long dealerId);

    boolean existsByIdAndLinkedDealers_Id(Long id, Long dealerId);
}
