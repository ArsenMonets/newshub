package com.github.arsenmonets.newshub.utils;

import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.dto.NewsDTO;
import com.github.arsenmonets.newshub.dto.NewsPreviewDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.NewsEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class NewsHubMapper {

    public UserDTO toUserDTO(UserEntity entity) {
        if (entity == null)
            return null;
        return new UserDTO(entity.getId(), entity.getLogin(), entity.getRole(), entity.isBlocked());
    }

    public CategoryDTO toCategoryDTO(CategoryEntity entity) {
        if (entity == null)
            return null;
        return new CategoryDTO(entity.getId(), entity.getName());
    }

    public NewsPreviewDTO toNewsPreviewDTO(NewsEntity entity) {
        if (entity == null)
            return null;
        return new NewsPreviewDTO(
                entity.getId(),
                entity.getTitle(),
                getSummary(entity.getContent()),
                toUserDTO(entity.getAuthor()),
                toCategoryDTO(entity.getCategory()),
                entity.getCreatedAt());
    }

    public NewsDTO toNewsDTO(NewsEntity entity) {
        if (entity == null)
            return null;
        return new NewsDTO(
                entity.getId(),
                entity.getTitle(),
                getSummary(entity.getContent()),
                entity.getContent(),
                toUserDTO(entity.getAuthor()),
                toCategoryDTO(entity.getCategory()),
                entity.getCreatedAt());
    }

    private String getSummary(String content) {
        if (content == null)
            return "";
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }
}
