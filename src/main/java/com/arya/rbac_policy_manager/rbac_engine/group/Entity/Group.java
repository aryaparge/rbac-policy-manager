package com.arya.rbac_policy_manager.rbac_engine.group.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "groups")//GROUP is a SQL reserved keyword. H2 hates the group table name.
public class Group extends BaseEntity { }
