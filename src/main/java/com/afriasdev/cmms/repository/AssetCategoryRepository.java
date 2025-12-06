package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    List<AssetCategory> findByIsActiveTrue();
    Optional<AssetCategory> findByCode(String code);
    List<AssetCategory> findByNameContainingIgnoreCase(String name);
}
