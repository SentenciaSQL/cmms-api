package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    // ========== MÉTODOS SIN PAGINACIÓN ==========
    List<Asset> findBySiteId(Long siteId);
    List<Asset> findByCategoryId(Long categoryId);
    List<Asset> findByIsActiveTrue();
    Optional<Asset> findBySiteIdAndCode(Long siteId, String code);

    @Query("SELECT a FROM Asset a WHERE a.site.company.id = :companyId")
    List<Asset> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT a FROM Asset a WHERE " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Asset> searchAssets(@Param("search") String search);

    // ========== MÉTODOS CON PAGINACIÓN (nombres diferentes) ==========
    Page<Asset> findAllBySiteId(Long siteId, Pageable pageable);
    Page<Asset> findAllByCategoryId(Long categoryId, Pageable pageable);
    Page<Asset> findAllByIsActiveTrue(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.site.company.id = :companyId")
    Page<Asset> findPageByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Asset> searchAssetsPage(@Param("search") String search, Pageable pageable);
}
