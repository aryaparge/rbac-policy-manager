package com.arya.rbac_policy_manager.rbac_engine.group.Entity;

import com.arya.rbac_policy_manager.rbac_engine.common.Entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "groups")
public class Group extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
