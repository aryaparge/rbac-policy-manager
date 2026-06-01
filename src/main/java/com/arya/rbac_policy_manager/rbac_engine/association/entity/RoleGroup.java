package com.arya.rbac_policy_manager.rbac_engine.association.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "role_group", uniqueConstraints = { @UniqueConstraint(columnNames = { "role_id", "group_id" }) })
public class RoleGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
}
