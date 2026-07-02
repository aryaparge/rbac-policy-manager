package com.arya.rbac_policy_manager.auth.security;

import com.arya.rbac_policy_manager.platformuser.entity.PlatformUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security doesn't understand PlatformUser - it understands UserDetails.
 * This class bridges the two.
 */
public class PlatformUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final String role;

    public PlatformUserDetails(PlatformUser platformUser) {
        this.id = platformUser.getId();
        this.username = platformUser.getUsername();
        this.passwordHash = platformUser.getPasswordHash();
        this.enabled = platformUser.isEnabled();
        this.role = platformUser.getRole().name();
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
