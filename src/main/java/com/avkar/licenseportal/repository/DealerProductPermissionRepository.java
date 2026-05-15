package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.DealerProductPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DealerProductPermissionRepository extends JpaRepository<DealerProductPermission, Long> {
    @Query("""
            SELECT dpp FROM DealerProductPermission dpp
            JOIN FETCH dpp.product p
            LEFT JOIN FETCH dpp.grantedBy
            WHERE dpp.dealer.id = :dealerId
            ORDER BY p.name ASC
            """)
    List<DealerProductPermission> findByDealerIdWithDetails(@Param("dealerId") Long dealerId);

    Optional<DealerProductPermission> findByDealer_IdAndProduct_Id(Long dealerId, Long productId);

    boolean existsByDealer_IdAndProduct_Id(Long dealerId, Long productId);

    void deleteByDealer_IdAndProduct_Id(Long dealerId, Long productId);
}
