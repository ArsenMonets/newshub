package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.SubscriptionsDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.exceptions.BadRequestException;
import com.github.arsenmonets.newshub.exceptions.ResourceNotFoundException;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NewsHubMapper mapper;

    @InjectMocks
    private UserService userService;

    private UserEntity user;
    private UserEntity author;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity("reader1", "reader@example.com", "password", UserRole.USER);
        user.setId(1L);
        user.setSubscribedCategories(new ArrayList<>());
        user.setSubscribedAuthors(new ArrayList<>());

        author = new UserEntity("author1", "author@example.com", "password", UserRole.AUTHOR);
        author.setId(2L);

        category = new CategoryEntity("Technology");
        category.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(userRepository.getReferenceById(2L)).thenReturn(author);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(categoryRepository.getReferenceById(1L)).thenReturn(category);
        when(userRepository.isSubscribedToCategory(1L, 1L)).thenReturn(false);
        when(userRepository.isSubscribedToAuthor(1L, 2L)).thenReturn(false);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
    }

    @Test
    @DisplayName("Should subscribe user to category successfully")
    void testSubscribeToCategorySuccess() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.getReferenceById(1L)).thenReturn(category);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(category)).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        userService.subscribeToCategory(userDetails, 1L);

        verify(categoryRepository).existsById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during category subscription")
    void testSubscribeToCategoryFailsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        user.setId(999L);
        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.subscribeToCategory(userDetails, 1L));
    }

    @Test
    @DisplayName("Should throw exception when category not found during subscription")
    void testSubscribeToCategoryFailsCategoryNotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.subscribeToCategory(userDetails, 999L));
    }

    @Test
    @DisplayName("Should unsubscribe user from category successfully")
    void testUnsubscribeFromCategorySuccess() {
        user.setSubscribedCategories(new ArrayList<>(Arrays.asList(category)));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(category)).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        userService.unsubscribeFromCategory(userDetails, 1L);

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when unsubscribing from non-subscribed category")
    void testUnsubscribeFromCategoryNotSubscribed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(category)).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        SubscriptionsDTO result = userService.unsubscribeFromCategory(userDetails, 1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should subscribe user to author successfully")
    void testSubscribeToAuthorSuccess() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.isSubscribedToAuthor(1L, 2L)).thenReturn(false);
        when(userRepository.getReferenceById(2L)).thenReturn(author);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(any(CategoryEntity.class))).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        userService.subscribeToAuthor(userDetails, 2L);

        verify(userRepository).existsById(2L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when subscribing to self as author")
    void testSubscribeToAuthorFailsSelfSubscribe() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        assertThrows(BadRequestException.class,
                () -> userService.subscribeToAuthor(userDetails, 1L));
    }

    @Test
    @DisplayName("Should throw exception when author not found")
    void testSubscribeToAuthorFailsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.subscribeToAuthor(userDetails, 999L));
    }

    @Test
    @DisplayName("Should allow subscribing to non-author user (returns user dto)")
    void testSubscribeToAuthorWithNonAuthorRole() {
        UserEntity nonAuthor = new UserEntity("reader2", "reader2@example.com", "password", UserRole.USER);
        nonAuthor.setId(3L);
        nonAuthor.setSubscribedCategories(new ArrayList<>());
        nonAuthor.setSubscribedAuthors(new ArrayList<>());

        when(userRepository.existsById(3L)).thenReturn(true);
        when(userRepository.getReferenceById(3L)).thenReturn(nonAuthor);
        when(userRepository.isSubscribedToAuthor(1L, 3L)).thenReturn(false);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(3L, "reader2", UserRole.USER, false);
        when(mapper.toCategoryDTO(any(CategoryEntity.class))).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        userService.subscribeToAuthor(userDetails, 3L);

        verify(userRepository).existsById(3L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should unsubscribe user from author successfully")
    void testUnsubscribeFromAuthorSuccess() {
        user.setSubscribedAuthors(new ArrayList<>(Arrays.asList(author)));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(any(CategoryEntity.class))).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        userService.unsubscribeFromAuthor(userDetails, 2L);

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when unsubscribing from non-subscribed author")
    void testUnsubscribeFromAuthorNotSubscribed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(any(CategoryEntity.class))).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        SubscriptionsDTO result = userService.unsubscribeFromAuthor(userDetails, 2L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get user subscriptions successfully")
    void testGetSubscriptionsSuccess() {
        user.setSubscribedCategories(new ArrayList<>(Arrays.asList(category)));
        user.setSubscribedAuthors(new ArrayList<>(Arrays.asList(author)));

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(category)).thenReturn(categoryDTO);
        when(mapper.toUserDTO(author)).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        SubscriptionsDTO subscriptions = userService.getSubscriptions(userDetails);

        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.categories().size());
        assertEquals(1, subscriptions.authors().size());
    }

    @Test
    @DisplayName("Should return empty subscriptions when user has no subscriptions")
    void testGetSubscriptionsEmpty() {
        when(userRepository.existsById(1L)).thenReturn(true);

        CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
        UserDTO userDTO = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        when(mapper.toCategoryDTO(any(CategoryEntity.class))).thenReturn(categoryDTO);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        AuthenticatedUserDTO userDetails = new AuthenticatedUserDTO(user.getId(), user.getLogin(), user.getRole());

        SubscriptionsDTO subscriptions = userService.getSubscriptions(userDetails);

        assertNotNull(subscriptions);
        assertEquals(0, subscriptions.categories().size());
        assertEquals(0, subscriptions.authors().size());
    }

    @Test
    @DisplayName("Should block user successfully")
    void testUpdateUserStatusBlockSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDTO userDTO = new UserDTO(1L, "reader1", UserRole.USER, true);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        userService.updateUserStatus(1L, true);

        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(u -> u.isBlocked() == true));
        verify(mapper).toUserDTO(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should unblock user successfully")
    void testUpdateUserStatusUnblockSuccess() {
        user.setBlocked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDTO userDTO = new UserDTO(1L, "reader1", UserRole.USER, false);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        userService.updateUserStatus(1L, false);

        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(u -> u.isBlocked() == false));
        verify(mapper).toUserDTO(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when updating status of non-existent user")
    void testUpdateUserStatusFailsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserStatus(999L, true));
    }

    @Test
    @DisplayName("Should change user role successfully")
    void testChangeRoleSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDTO userDTO = new UserDTO(1L, "reader1", UserRole.AUTHOR, false);
        when(mapper.toUserDTO(any(UserEntity.class))).thenReturn(userDTO);

        userService.changeRole(1L, UserRole.AUTHOR.name());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
        verify(mapper).toUserDTO(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when changing role of non-existent user")
    void testChangeRoleFailsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.changeRole(999L, UserRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("Should get user data successfully")
    void testGetUserDataSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO userDTO = new UserDTO(1L, "reader1", UserRole.USER, false);
        when(mapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO userData = userService.getUserData(1L);

        assertNotNull(userData);
        assertEquals(1L, userData.id());
        assertEquals("reader1", userData.login());
        assertFalse(userData.isBlocked());
        verify(userRepository).findById(1L);
        verify(mapper).toUserDTO(user);
    }

    @Test
    @DisplayName("Should throw exception when getting data of non-existent user")
    void testGetUserDataFailsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserData(999L));
    }

    @Test
    @DisplayName("Should throw exception when changing to invalid role")
    void testChangeRoleFailsInvalidRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> userService.changeRole(1L, "INVALID_ROLE"));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all authors successfully")
    void testGetAllAuthorsSuccess() {
        UserEntity author2 = new UserEntity("author2", "author2@example.com", "password", UserRole.AUTHOR);
        author2.setId(3L);

        List<UserEntity> authors = Arrays.asList(author, author2);
        when(userRepository.findAllAuthors()).thenReturn(authors);

        UserDTO authorDTO1 = new UserDTO(2L, "author1", UserRole.AUTHOR, false);
        UserDTO authorDTO2 = new UserDTO(3L, "author2", UserRole.AUTHOR, false);
        when(mapper.toUserDTO(author)).thenReturn(authorDTO1);
        when(mapper.toUserDTO(author2)).thenReturn(authorDTO2);

        List<UserDTO> result = userService.getAllAuthors();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAllAuthors();
        verify(mapper, times(2)).toUserDTO(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should return empty list when no authors exist")
    void testGetAllAuthorsEmpty() {
        when(userRepository.findAllAuthors()).thenReturn(Arrays.asList());

        List<UserDTO> result = userService.getAllAuthors();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository).findAllAuthors();
    }

    @Test
    @DisplayName("Should get all non-admin users with login filter successfully")
    void testGetAllNonAdminsWithLoginFilterSuccess() {
        UserEntity author = new UserEntity("testauthor", "author@example.com", "password", UserRole.AUTHOR);
        author.setId(3L);
        author.setBlocked(false);

        List<UserEntity> users = Arrays.asList(user, author);
        Page<UserEntity> userPage = new PageImpl<>(users);

        when(userRepository.findAllNonAdminsWithLoginFilter("test",
                org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(userPage);

        UserDTO userDTO1 = new UserDTO(1L, "testuser", UserRole.USER, false);
        UserDTO userDTO2 = new UserDTO(3L, "testauthor", UserRole.AUTHOR, false);

        when(mapper.toUserDTO(user)).thenReturn(userDTO1);
        when(mapper.toUserDTO(author)).thenReturn(userDTO2);

        Page<UserDTO> result = userService.getAllNonAdminsWithLoginFilter("test",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(userRepository).findAllNonAdminsWithLoginFilter("test",
                org.springframework.data.domain.PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Should return empty page when no non-admin users match filter")
    void testGetAllNonAdminsWithLoginFilterEmpty() {
        Page<UserEntity> emptyPage = new PageImpl<>(Arrays.asList());

        when(userRepository.findAllNonAdminsWithLoginFilter("nonexistent",
                org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        Page<UserDTO> result = userService.getAllNonAdminsWithLoginFilter("nonexistent",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository).findAllNonAdminsWithLoginFilter("nonexistent",
                org.springframework.data.domain.PageRequest.of(0, 10));
    }
}
