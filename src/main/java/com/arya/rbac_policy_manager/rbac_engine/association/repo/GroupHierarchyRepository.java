package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupHierarchyRepository extends JpaRepository<GroupHierarchy, UUID> {

        boolean existsByParentGroupAndChildGroup(Group parentGroup, Group childGroup);

        List<GroupHierarchy> findByParentGroupAndStatus(Group parentGroup, Status status);

        List<GroupHierarchy> findByChildGroupAndStatus(Group childGroup, Status status);

        Optional<GroupHierarchy> findByParentGroupAndChildGroup(Group parentGroup, Group childGroup);
}
