package com.afriasdev.cmms.dto;

import com.afriasdev.cmms.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String email;
    private String phone;
    private String mobile;
    private String website;
    private String taxId;
    private AddressDTO address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private Supplier.SupplierType supplierType;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
