package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // ========== MÉTODOS SIN PAGINACIÓN ==========
    List<Company> findByIsActiveTrue();
    Optional<Company> findByTaxId(String taxId);
    List<Company> findByNameContainingIgnoreCase(String name);

    // ========== MÉTODOS CON PAGINACIÓN (nombres diferentes) ==========
    Page<Company> findAllByIsActiveTrue(Pageable pageable);
    Page<Company> findPageByNameContainingIgnoreCase(String name, Pageable pageable);
}
