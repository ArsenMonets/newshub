package com.github.arsenmonets.newshub.dto;

import java.time.LocalDateTime;

public record NewsDTO(
        Long id,
        String title,
        String summary,
        String content,
        UserDTO author,
        CategoryDTO category,
        LocalDateTime createdAt) {
}
