package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.security.JwtUtil;
import com.github.arsenmonets.newshub.websocket.WebSocketConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("AdminController Integration Tests")
@TestPropertySource(locations = "classpath:application-integration.properties")
@Import(WebSocketConfig.class)
class AdminControllerIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String userToken;
    private UserEntity testUser;

    private UserEntity testAdmin;

    private static final String ADMIN_BLOCK_USER_URL = "/api/v1/admin/users/{userId}/block";
    private static final String ADMIN_UNBLOCK_USER_URL = "/api/v1/admin/users/{userId}/unblock";
    private static final String ADMIN_CHANGE_ROLE_URL = "/api/v1/admin/users/{userId}/role";
    private static final String ADMIN_GET_NON_ADMIN_USERS_URL = "/api/v1/admin/users";
    private static final String ADMIN_ADD_CATEGORY_URL = "/api/v1/admin/categories";
    private static final String ADMIN_UPDATE_CATEGORY_URL = "/api/v1/admin/categories/{id}";
    private static final String ADMIN_DELETE_CATEGORY_URL = "/api/v1/admin/categories/{id}";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        testAdmin = new UserEntity("testadmin2", "testadmin2@example.com", "password", UserRole.ADMIN);
        testAdmin.setBlocked(false);
        testAdmin = userRepository.save(testAdmin);
        adminToken = jwtUtil.generateToken(testAdmin.getLogin(), testAdmin.getRole().name(), testAdmin.getId());

        testUser = new UserEntity("testuser", "user@example.com", "password", UserRole.USER);
        testUser.setBlocked(false);
        testUser = userRepository.save(testUser);
        userToken = jwtUtil.generateToken(testUser.getLogin(), testUser.getRole().name(), testUser.getId());
    }

    @Test
    @DisplayName("Block user success")
    void blockUserSuccess() throws Exception {
        mockMvc.perform(post(ADMIN_BLOCK_USER_URL, testUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlocked").value(true));
    }

    @Test
    @DisplayName("Block user not found")
    void blockUserNotFound() throws Exception {
        mockMvc.perform(post(ADMIN_BLOCK_USER_URL, 999)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Block user without admin role fails")
    void blockUserWithoutAdminRoleFails() throws Exception {
        mockMvc.perform(post(ADMIN_BLOCK_USER_URL, testUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Block user without authentication fails")
    void blockUserWithoutAuthenticationFails() throws Exception {
        mockMvc.perform(post(ADMIN_BLOCK_USER_URL, testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unblock user success")
    void unblockUserSuccess() throws Exception {
        testUser.setBlocked(true);
        userRepository.save(testUser);

        mockMvc.perform(post(ADMIN_UNBLOCK_USER_URL, testUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlocked").value(false));
    }

    @Test
    @DisplayName("Unblock user not found")
    void unblockUserNotFound() throws Exception {
        mockMvc.perform(post(ADMIN_UNBLOCK_USER_URL, 999)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Change user role to author success")
    void changeUserRoleToAuthorSuccess() throws Exception {
        mockMvc.perform(put(ADMIN_CHANGE_ROLE_URL, testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .param("newRole", "AUTHOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AUTHOR"));
    }

    @Test
    @DisplayName("Change user role to admin success")
    void changeUserRoleToAdminSuccess() throws Exception {
        mockMvc.perform(put(ADMIN_CHANGE_ROLE_URL, testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .param("newRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Change user role user not found")
    void changeUserRoleUserNotFound() throws Exception {
        mockMvc.perform(put(ADMIN_CHANGE_ROLE_URL, 999)
                .header("Authorization", "Bearer " + adminToken)
                .param("newRole", "AUTHOR"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Change user role with invalid role")
    void changeUserRoleWithInvalidRole() throws Exception {
        mockMvc.perform(put(ADMIN_CHANGE_ROLE_URL, testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .param("newRole", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Add category success")
    void addCategorySuccess() throws Exception {
        mockMvc.perform(post(ADMIN_ADD_CATEGORY_URL)
                .header("Authorization", "Bearer " + adminToken)
                .param("name", "Technology"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Technology"));
    }

    @Test
    @DisplayName("Add category without admin role fails")
    void addCategoryWithoutAdminRoleFails() throws Exception {
        mockMvc.perform(post(ADMIN_ADD_CATEGORY_URL)
                .header("Authorization", "Bearer " + userToken)
                .param("name", "Technology"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Add category without authentication fails")
    void addCategoryWithoutAuthenticationFails() throws Exception {
        mockMvc.perform(post(ADMIN_ADD_CATEGORY_URL)
                .param("name", "Technology"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Add duplicate category fails")
    void addDuplicateCategoryFails() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        categoryRepository.save(category);

        mockMvc.perform(post(ADMIN_ADD_CATEGORY_URL)
                .header("Authorization", "Bearer " + adminToken)
                .param("name", "Technology"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Update category success")
    void updateCategorySuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("OldName");
        category = categoryRepository.save(category);

        mockMvc.perform(put(ADMIN_UPDATE_CATEGORY_URL, category.getId())
                .header("Authorization", "Bearer " + adminToken)
                .param("name", "NewName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    @DisplayName("Update category not found")
    void updateCategoryNotFound() throws Exception {
        mockMvc.perform(put(ADMIN_UPDATE_CATEGORY_URL, 999)
                .header("Authorization", "Bearer " + adminToken)
                .param("name", "NewName"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update category without admin role fails")
    void updateCategoryWithoutAdminRoleFails() throws Exception {
        CategoryEntity category = new CategoryEntity("OldName");
        category = categoryRepository.save(category);

        mockMvc.perform(put(ADMIN_UPDATE_CATEGORY_URL, category.getId())
                .header("Authorization", "Bearer " + userToken)
                .param("name", "NewName"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete category success")
    void deleteCategorySuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("ToDelete");
        category = categoryRepository.save(category);

        mockMvc.perform(delete(ADMIN_DELETE_CATEGORY_URL, category.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete category not found")
    void deleteCategoryNotFound() throws Exception {
        mockMvc.perform(delete(ADMIN_DELETE_CATEGORY_URL, 999)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete category without admin role fails")
    void deleteCategoryWithoutAdminRoleFails() throws Exception {
        CategoryEntity category = new CategoryEntity("ToDelete");
        category = categoryRepository.save(category);

        mockMvc.perform(delete(ADMIN_DELETE_CATEGORY_URL, category.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete category without authentication fails")
    void deleteCategoryWithoutAuthenticationFails() throws Exception {
        CategoryEntity category = new CategoryEntity("ToDelete");
        category = categoryRepository.save(category);

        mockMvc.perform(delete(ADMIN_DELETE_CATEGORY_URL, category.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get all non-admin users with login filter success")
    void getAllNonAdminUsersWithLoginFilterSuccess() throws Exception {
        UserEntity author = new UserEntity("author1", "author1@example.com", "password", UserRole.AUTHOR);
        author.setBlocked(false);
        userRepository.save(author);

        UserEntity anotherUser = new UserEntity("reader2", "reader2@example.com", "password", UserRole.USER);
        anotherUser.setBlocked(false);
        userRepository.save(anotherUser);

        mockMvc.perform(get(ADMIN_GET_NON_ADMIN_USERS_URL)
                .header("Authorization", "Bearer " + adminToken)
                .param("loginFilter", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].login").value("testuser"));
    }

    @Test
    @DisplayName("Get all non-admin users with pagination success")
    void getAllNonAdminUsersWithPaginationSuccess() throws Exception {
        for (int i = 0; i < 5; i++) {
            UserEntity user = new UserEntity("user" + i, "user" + i + "@example.com", "password", UserRole.USER);
            user.setBlocked(false);
            userRepository.save(user);
        }

        mockMvc.perform(get(ADMIN_GET_NON_ADMIN_USERS_URL)
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get all non-admin users without admin role fails")
    void getAllNonAdminUsersWithoutAdminRoleFails() throws Exception {
        mockMvc.perform(get(ADMIN_GET_NON_ADMIN_USERS_URL)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all non-admin users without authentication fails")
    void getAllNonAdminUsersWithoutAuthenticationFails() throws Exception {
        mockMvc.perform(get(ADMIN_GET_NON_ADMIN_USERS_URL))
                .andExpect(status().isUnauthorized());
    }
}
