package com.arya.rbac_policy_manager.rbac_engine.association.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "group_permission" , uniqueConstraints = { @UniqueConstraint(columnNames = { "group_id" , "permission_id" }) })
public class GroupPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}
