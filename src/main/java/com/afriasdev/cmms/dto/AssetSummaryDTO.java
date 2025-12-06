package com.afriasdev.cmms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetSummaryDTO {
    private Long id;
    private String code;
    private String name;
    private String categoryName;
    private String siteName;
    private String companyName;
    private String manufacturer;
    private String model;
    private LocalDate installedAt;
    private Boolean isActive;

    private Integer totalWorkOrders;
    private Integer openWorkOrders;
}
