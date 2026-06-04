package com.arya.rbac_policy_manager.rbac_engine.association.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "role_hierarchy" , uniqueConstraints = { @UniqueConstraint(columnNames = { "parent_role_id" , "child_role_id" }) })
public class RoleHierarchy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_role_id", nullable = false)
    private Role parentRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_role_id", nullable = false)
    private Role childRole;
}

