package com.arya.rbac_policy_manager.rbac_engine.subject.Entity;

import com.arya.rbac_policy_manager.rbac_engine.common.Entity.BaseEntity;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.SubjectType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "subject")
public class Subject extends BaseEntity {

    @Column(unique = true, nullable = false, updatable = false)
    private String subjectID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @Column(nullable = false)
    private String displayName;
}
