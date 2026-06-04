package com.arya.rbac_policy_manager.rbac_engine.subject.entity;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.SubjectType;
import com.arya.rbac_policy_manager.rbac_engine.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "subject")
public class Subject extends BaseEntity {

    @Column(unique = true, nullable = false, updatable = false)
    private String subjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @Column(nullable = false)
    private String displayName;

    private String description;
}
