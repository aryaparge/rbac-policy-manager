package com.arya.rbac_policy_manager.rbac_engine.association.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "group_hierarchy" , uniqueConstraints = { @UniqueConstraint(columnNames = { "parent_group_id" , "child_group_id" }) })
public class GroupHierarchy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_group_id", nullable = false)
    private Group parentGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_group_id", nullable = false)
    private Group childGroup;
}

