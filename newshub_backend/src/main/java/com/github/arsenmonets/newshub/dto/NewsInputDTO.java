package com.github.arsenmonets.newshub.dto;

public record NewsInputDTO(
        String title,
        String content,
        Long categoryId) {
}
