package com.github.arsenmonets.newshub.dto;

import java.time.LocalDateTime;

public record NewsPreviewDTO(
        Long id,
        String title,
        String summary,
        UserDTO author,
        CategoryDTO category,
        LocalDateTime createdAt) {
}