package com.afriasdev.cmms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetFilterRequest {

    private Long companyId;
    private Long siteId;
    private Long categoryId;

    private String manufacturer;
    private String model;

    private Boolean isActive;

    private String search; // Buscar en nombre, código o serial

    // Paginación
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "name";
    private String sortDirection = "ASC";
}
