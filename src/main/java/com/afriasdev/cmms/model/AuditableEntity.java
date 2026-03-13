package com.afriasdev.cmms.model;

import com.afriasdev.cmms.security.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class para todas las entidades auditables del CMMS.
 *
 * Provee automáticamente:
 *  - createdAt  : fecha de creación   (solo se escribe una vez)
 *  - updatedAt  : fecha de última modificación
 *  - createdBy  : usuario que creó el registro
 *  - updatedBy  : último usuario que modificó el registro
 *
 * Spring Data JPA los rellena via AuditorAwareImpl en cada save/merge,
 * sin necesidad de ningún código en los servicios.
 *
 * Uso:
 *  @Entity
 *  public class MyEntity extends AuditableEntity { ... }
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
