package com.afriasdev.cmms.dto.request;

import com.afriasdev.cmms.dto.AddressDTO;
import com.afriasdev.cmms.model.Supplier;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateRequest {

    @NotBlank(message = "El nombre es requerido")
    private String name;

    private String code;
    private String description;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
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
}
