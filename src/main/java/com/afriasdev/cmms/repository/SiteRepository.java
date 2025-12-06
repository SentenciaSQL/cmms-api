package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByCompanyId(Long companyId);
    List<Site> findByIsActiveTrue();
    List<Site> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<Site> findByCompanyIdAndCode(Long companyId, String code);
}
