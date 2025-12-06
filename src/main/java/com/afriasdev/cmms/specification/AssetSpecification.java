package com.afriasdev.cmms.specification;

import com.afriasdev.cmms.model.Asset;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {

    public static Specification<Asset> withFilters(
            Long companyId,
            Long siteId,
            Long categoryId,
            String manufacturer,
            String model,
            Boolean isActive,
            String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtrar por empresa
            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("site").get("company").get("id"), companyId));
            }

            // Filtrar por sitio
            if (siteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("site").get("id"), siteId));
            }

            // Filtrar por categoría
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filtrar por fabricante
            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("manufacturer")),
                        "%" + manufacturer.toLowerCase() + "%"));
            }

            // Filtrar por modelo
            if (model != null && !model.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")),
                        "%" + model.toLowerCase() + "%"));
            }

            // Filtrar por estado activo
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            // Búsqueda en nombre, código o número de serie
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate codeMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")), searchPattern);
                Predicate serialMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serialNumber")), searchPattern);

                predicates.add(criteriaBuilder.or(nameMatch, codeMatch, serialMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}