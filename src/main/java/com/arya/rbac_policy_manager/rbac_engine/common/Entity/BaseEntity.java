package com.arya.rbac_policy_manager.rbac_engine.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;

import java.time.Instant;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
    private String updatedBy;

    private Instant disabledAt;

    private Instant deletedAt;
}
