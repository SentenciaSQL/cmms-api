package com.afriasdev.cmms.repository;

import com.afriasdev.cmms.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    List<Customer> findByCompanyId(Long companyId);
    List<Customer> findByIsActiveTrue();
}
