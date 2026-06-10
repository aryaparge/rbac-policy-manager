package com.arya.rbac_policy_manager.rbac_engine.resource.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "resource")
public class Resource extends BaseEntity {
    @Column(nullable = false)
    private String displayName;
}
