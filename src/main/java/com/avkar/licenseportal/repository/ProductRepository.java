package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    List<Product> findByActiveTrueOrderByNameAsc();

    Page<Product> findAllByOrderByNameAsc(Pageable pageable);

    @Query(
            value = """
                    SELECT p FROM Product p
                    WHERE (:q IS NULL OR :q = ''
                           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    ORDER BY p.name ASC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Product p
                    WHERE (:q IS NULL OR :q = ''
                           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :q, '%')))
                    """
    )
    Page<Product> searchPage(@Param("q") String q, Pageable pageable);
}

