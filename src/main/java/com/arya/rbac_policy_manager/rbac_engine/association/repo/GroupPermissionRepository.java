package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, UUID> {

    List<GroupPermission> findByGroup(Group group);

    List<GroupPermission> findByGroupAndStatus(Group group, Status status);

    List<GroupPermission> findByPermission(Permission permission);

    boolean existsByGroupAndPermission(Group group, Permission permission);
}