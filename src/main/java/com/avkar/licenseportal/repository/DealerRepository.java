package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealerRepository extends JpaRepository<Dealer, Long> {
    Page<Dealer> findAllByOrderByNameAsc(Pageable pageable);

    @Query(
            value = """
                    SELECT d FROM Dealer d
                    WHERE (:q IS NULL OR :q = ''
                           OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.phone, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    ORDER BY d.name ASC
                    """,
            countQuery = """
                    SELECT COUNT(d) FROM Dealer d
                    WHERE (:q IS NULL OR :q = ''
                           OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.contactName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(d.phone, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    """
    )
    Page<Dealer> searchPage(@Param("q") String q, Pageable pageable);
}

