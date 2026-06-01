package com.arya.rbac_policy_manager.rbac_engine.principal.Entity;

import com.arya.rbac_policy_manager.rbac_engine.common.Entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.PrincipalType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "principals") //PRINCIPAL can be a keyword in some databases and security frameworks.
public class Principal extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrincipalType principalType;

    @Column(nullable = false)
    private String displayName;
}
