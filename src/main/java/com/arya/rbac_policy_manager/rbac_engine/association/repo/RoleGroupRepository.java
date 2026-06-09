package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleGroupRepository extends JpaRepository<RoleGroup, UUID> {

    List<RoleGroup> findByRole(Role role);

    List<RoleGroup> findByRoleAndStatus(Role role, Status status);

    List<RoleGroup> findByGroup(Group group);

    boolean existsByRoleAndGroup( Role role, Group group);
}