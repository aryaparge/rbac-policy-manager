package com.arya.rbac_policy_manager.auth.security;

import com.arya.rbac_policy_manager.platformuser.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformUserDetailsService implements UserDetailsService {

    private final PlatformUserRepository platformUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return platformUserRepository.findByUsername(username)
                .map(PlatformUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No platform user found with username: " + username));
    }
}
