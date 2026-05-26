package com.github.arsenmonets.newshub.dto;

import com.github.arsenmonets.newshub.models.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public record AuthenticatedUserDTO(
        Long id,
        String username,
        UserRole role) implements Serializable {

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
