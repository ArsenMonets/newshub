package com.github.arsenmonets.newshub.dto;

import java.util.List;

public record SubscriptionsDTO(
                List<CategoryDTO> categories,
                List<UserDTO> authors) {
}
