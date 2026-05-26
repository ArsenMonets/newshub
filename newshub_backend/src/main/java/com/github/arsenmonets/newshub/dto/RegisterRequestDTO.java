package com.github.arsenmonets.newshub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Login не може бути пустим") String login,

        @NotBlank(message = "Email не може бути пустим") @Email(message = "Email має невірний формат") String email,

        @NotBlank(message = "Пароль не може бути пустим") @Size(min = 6, message = "Пароль має бути мінімум 6 символів") String password) {
}
