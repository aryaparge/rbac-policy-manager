package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, UUID> {

        boolean existsByParentRoleAndChildRole(Role parentRole, Role childRole);
        
        boolean existsByParentRoleAndChildRoleAndStatus( Role parent, Role child, Status status );

        List<RoleHierarchy> findByParentRoleAndStatus(Role parentRole, Status status);

        List<RoleHierarchy> findByChildRoleAndStatus(Role childRole, Status status);

        Optional<RoleHierarchy> findByParentRoleAndChildRole(Role parentRole, Role childRole); 
}
