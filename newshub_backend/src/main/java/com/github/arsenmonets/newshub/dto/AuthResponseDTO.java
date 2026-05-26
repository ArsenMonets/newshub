package com.github.arsenmonets.newshub.dto;

public record AuthResponseDTO(
        String token,
        UserDTO user) {
}
