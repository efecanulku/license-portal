package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            WHERE (:dealerId IS NULL OR c.createdByDealer.id = :dealerId)
            ORDER BY c.name ASC
            """)
    List<Customer> findAllWithOptionalDealerFilter(@Param("dealerId") Long dealerId);

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            WHERE c.createdByDealer.id = :dealerId
            ORDER BY c.name ASC
            """)
    List<Customer> findByCreatedByDealerId(@Param("dealerId") Long dealerId);

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            WHERE c.id = :id
            """)
    Optional<Customer> findByIdWithDealer(@Param("id") Long id);

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.createdByDealer
            WHERE c.id = :id AND c.createdByDealer.id = :dealerId
            """)
    Optional<Customer> findByIdAndCreatedByDealerId(@Param("id") Long id, @Param("dealerId") Long dealerId);
}
