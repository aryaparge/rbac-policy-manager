package com.arya.rbac_policy_manager.platformuser.repository;

import com.arya.rbac_policy_manager.platformuser.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {

    Optional<PlatformUser> findByUsername(String username);
}
