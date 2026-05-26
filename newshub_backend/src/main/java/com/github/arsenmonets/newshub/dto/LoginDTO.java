package com.github.arsenmonets.newshub.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Login не може бути пустим") String login,

        @NotBlank(message = "Пароль не може бути пустим") String password) {
}
