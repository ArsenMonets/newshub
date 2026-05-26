package com.github.arsenmonets.newshub.dto;

import com.github.arsenmonets.newshub.models.UserRole;

public record UserDTO(
                Long id,
                String login,
                UserRole role,
                boolean isBlocked) {
}
