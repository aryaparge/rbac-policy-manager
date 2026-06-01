package com.arya.rbac_policy_manager.rbac_engine.permission.entity;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

import jakarta.persistence.*;

@Entity
@Table(name = "permission", uniqueConstraints = { @UniqueConstraint(columnNames = { "resource_id", "action_id" }) })
public class Permission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false) //lazy to avoid overfetching, still not convinced that eager isnt needed here.
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    private String description;
}
