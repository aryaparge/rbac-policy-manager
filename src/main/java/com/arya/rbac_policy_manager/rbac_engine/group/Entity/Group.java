package com.arya.rbac_policy_manager.rbac_engine.group.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "group")
public class Group extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
