package com.afriasdev.cmms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String mobile;

    private String website;

    @Column(length = 50)
    private String taxId; // RNC o Tax ID

    @Embedded
    private Address address;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    @Enumerated(EnumType.STRING)
    private SupplierType supplierType; // PARTS, TOOLS, SERVICES, MATERIALS

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SupplierType {
        PARTS, TOOLS, SERVICES, MATERIALS, GENERAL
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
