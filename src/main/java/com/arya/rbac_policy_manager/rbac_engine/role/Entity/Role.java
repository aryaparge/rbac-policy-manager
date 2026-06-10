package com.arya.rbac_policy_manager.rbac_engine.role.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "role")
public class Role extends BaseEntity { }
