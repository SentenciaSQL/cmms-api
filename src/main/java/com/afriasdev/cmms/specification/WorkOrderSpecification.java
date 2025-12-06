package com.afriasdev.cmms.specification;

import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderPriority;
import com.afriasdev.cmms.model.WorkOrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkOrderSpecification {

    public static Specification<WorkOrder> withFilters(
            List<WorkOrderStatus> statuses,
            List<WorkOrderPriority> priorities,
            Long companyId,
            Long siteId,
            Long assetId,
            Long assignedTechId,
            Long requesterId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            LocalDate createdFrom,
            LocalDate createdTo,
            Boolean overdue,
            Boolean unassigned,
            String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtrar por estados
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            // Filtrar por prioridades
            if (priorities != null && !priorities.isEmpty()) {
                predicates.add(root.get("priority").in(priorities));
            }

            // Filtrar por empresa
            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }

            // Filtrar por sitio
            if (siteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("site").get("id"), siteId));
            }

            // Filtrar por activo
            if (assetId != null) {
                predicates.add(criteriaBuilder.equal(root.get("asset").get("id"), assetId));
            }

            // Filtrar por técnico asignado
            if (assignedTechId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTech").get("id"), assignedTechId));
            }

            // Filtrar por solicitante
            if (requesterId != null) {
                predicates.add(criteriaBuilder.equal(root.get("requester").get("id"), requesterId));
            }

            // Filtrar por rango de fecha límite
            if (dueDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            // Filtrar por rango de fecha de creación
            if (createdFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), createdFrom.atStartOfDay()));
            }
            if (createdTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), createdTo.atTime(23, 59, 59)));
            }

            // Filtrar vencidas
            if (overdue != null && overdue) {
                predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), LocalDate.now()));
                predicates.add(root.get("status").in(WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS));
            }

            // Filtrar no asignadas
            if (unassigned != null && unassigned) {
                predicates.add(criteriaBuilder.isNull(root.get("assignedTech")));
            }

            // Búsqueda en título, descripción o código
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate codeMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")), searchPattern);

                predicates.add(criteriaBuilder.or(titleMatch, descMatch, codeMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
