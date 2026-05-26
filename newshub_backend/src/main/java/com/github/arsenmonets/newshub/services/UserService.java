package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.dto.SubscriptionsDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.exceptions.BadRequestException;
import com.github.arsenmonets.newshub.exceptions.ResourceNotFoundException;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NewsHubMapper mapper;

    public UserService(UserRepository userRepository, CategoryRepository categoryRepository, NewsHubMapper mapper) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public SubscriptionsDTO subscribeToCategory(AuthenticatedUserDTO currentUser, Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Категорію не знайдено");
        }

        if (!userRepository.isSubscribedToCategory(currentUser.id(), categoryId)) {
            UserEntity user = userRepository.getReferenceById(currentUser.id());
            CategoryEntity category = categoryRepository.getReferenceById(categoryId);
            user.getSubscribedCategories().add(category);
            userRepository.save(user);
        }
        return getSubscriptions(currentUser);
    }

    @Transactional
    public SubscriptionsDTO unsubscribeFromCategory(AuthenticatedUserDTO currentUser, Long categoryId) {
        UserEntity user = userRepository.getReferenceById(currentUser.id());
        user.getSubscribedCategories().removeIf(c -> c.getId().equals(categoryId));
        userRepository.save(user);
        return getSubscriptions(currentUser);
    }

    @Transactional
    public SubscriptionsDTO subscribeToAuthor(AuthenticatedUserDTO currentUser, Long authorId) {
        if (currentUser.id().equals(authorId)) {
            throw new BadRequestException("Ви не можете підписатися на самого себе");
        }

        if (!userRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Автора не знайдено");
        }

        if (!userRepository.isSubscribedToAuthor(currentUser.id(), authorId)) {
            UserEntity user = userRepository.getReferenceById(currentUser.id());
            UserEntity author = userRepository.getReferenceById(authorId);
            user.getSubscribedAuthors().add(author);
            userRepository.save(user);
        }
        return getSubscriptions(currentUser);
    }

    @Transactional
    public SubscriptionsDTO unsubscribeFromAuthor(AuthenticatedUserDTO currentUser, Long authorId) {
        UserEntity user = userRepository.getReferenceById(currentUser.id());
        user.getSubscribedAuthors().removeIf(a -> a.getId().equals(authorId));
        userRepository.save(user);
        return getSubscriptions(currentUser);
    }

    public SubscriptionsDTO getSubscriptions(AuthenticatedUserDTO currentUser) {
        if (!userRepository.existsById(currentUser.id())) {
            throw new ResourceNotFoundException("Користувача не знайдено");
        }
        UserEntity user = userRepository.getReferenceById(currentUser.id());
        List<CategoryDTO> categories = user.getSubscribedCategories().stream()
                .map(mapper::toCategoryDTO)
                .toList();

        List<UserDTO> authors = user.getSubscribedAuthors().stream()
                .map(mapper::toUserDTO)
                .toList();

        return new SubscriptionsDTO(categories, authors);
    }

    @Transactional
    public UserDTO updateUserStatus(Long userId, boolean isBlocked) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
        user.setBlocked(isBlocked);
        return mapper.toUserDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO changeRole(Long userId, String newRole) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));

        try {
            UserRole role = UserRole.valueOf(newRole.toUpperCase());
            user.setRole(role);
            return mapper.toUserDTO(userRepository.save(user));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Невідома роль: " + newRole);
        }
    }

    public UserDTO getUserData(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
        return mapper.toUserDTO(user);
    }

    public List<UserDTO> getAllAuthors() {
        return userRepository.findAllAuthors().stream()
                .map(mapper::toUserDTO)
                .toList();
    }

    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
    }

    @Transactional
    public void addCategorySubscriptionById(Long userId, Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Категорію не знайдено");
        }
        UserEntity user = userRepository.getReferenceById(userId);
        CategoryEntity category = categoryRepository.getReferenceById(categoryId);
        if (user.getSubscribedCategories().stream().noneMatch(c -> c.getId().equals(categoryId))) {
            user.getSubscribedCategories().add(category);
            userRepository.save(user);
        }
    }

    @Transactional
    public void removeCategorySubscriptionById(Long userId, Long categoryId) {
        UserEntity user = userRepository.getReferenceById(userId);
        user.getSubscribedCategories().removeIf(c -> c.getId().equals(categoryId));
        userRepository.save(user);
    }

    @Transactional
    public void addAuthorSubscriptionById(Long userId, Long authorId) {
        if (userId.equals(authorId)) {
            throw new BadRequestException("Ви не можете підписатися на самого себе");
        }
        if (!userRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Автора не знайдено");
        }
        UserEntity user = userRepository.getReferenceById(userId);
        UserEntity author = userRepository.getReferenceById(authorId);
        if (user.getSubscribedAuthors().stream().noneMatch(a -> a.getId().equals(authorId))) {
            user.getSubscribedAuthors().add(author);
            userRepository.save(user);
        }
    }

    @Transactional
    public void removeAuthorSubscriptionById(Long userId, Long authorId) {
        UserEntity user = userRepository.getReferenceById(userId);
        user.getSubscribedAuthors().removeIf(a -> a.getId().equals(authorId));
        userRepository.save(user);
    }

    public Page<UserDTO> getAllNonAdminsWithLoginFilter(String loginFilter, Pageable pageable) {
        return userRepository.findAllNonAdminsWithLoginFilter(loginFilter, pageable)
                .map(mapper::toUserDTO);
    }
}
