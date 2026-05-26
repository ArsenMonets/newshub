package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("UserController Integration Tests")
@TestPropertySource(locations = "classpath:application-integration.properties")
class UserControllerIntegrationTests {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private JwtUtil jwtUtil;

        private String userToken;
        private UserEntity testUser;
        private static final String USER_SUBSCRIBE_CATEGORY_URL = "/api/v1/user/subscribe/category/{categoryId}";
        private static final String USER_UNSUBSCRIBE_CATEGORY_URL = "/api/v1/user/unsubscribe/category/{categoryId}";
        private static final String USER_SUBSCRIBE_AUTHOR_URL = "/api/v1/user/subscribe/author/{authorId}";
        private static final String USER_UNSUBSCRIBE_AUTHOR_URL = "/api/v1/user/unsubscribe/author/{authorId}";
        private static final String USER_SUBSCRIPTIONS_URL = "/api/v1/user/subscriptions";

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();
                userRepository.deleteAll();
                categoryRepository.deleteAll();

                testUser = new UserEntity("testuser", "test@example.com", "password", UserRole.USER);
                testUser.setBlocked(false);
                testUser = userRepository.save(testUser);
                userToken = jwtUtil.generateToken(testUser.getLogin(), testUser.getRole().name(), testUser.getId());
        }

        @Test
        @DisplayName("Subscribe to category success")
        void subscribeToCategory() throws Exception {
                CategoryEntity category = new CategoryEntity("Technology");
                category = categoryRepository.save(category);

                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, category.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(1)))
                                .andExpect(jsonPath("$.categories[0].name").value("Technology"));
        }

        @Test
        @DisplayName("Subscribe to category not found")
        void subscribeToNonExistentCategory() throws Exception {
                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, 999)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unsubscribe from category success")
        void unsubscribeFromCategory() throws Exception {
                CategoryEntity category = new CategoryEntity("Technology");
                category = categoryRepository.save(category);

                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, category.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk());

                mockMvc.perform(delete(USER_UNSUBSCRIBE_CATEGORY_URL, category.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(0)));
        }

        @Test
        @DisplayName("Subscribe to author success")
        void subscribeToAuthor() throws Exception {
                UserEntity author = new UserEntity("author", "author@example.com", "password", UserRole.AUTHOR);
                author.setBlocked(false);
                author = userRepository.save(author);

                mockMvc.perform(post(USER_SUBSCRIBE_AUTHOR_URL, author.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authors", hasSize(1)))
                                .andExpect(jsonPath("$.authors[0].login").value("author"));
        }

        @Test
        @DisplayName("Subscribe to self fails")
        void subscribeToSelfFails() throws Exception {
                mockMvc.perform(post(USER_SUBSCRIBE_AUTHOR_URL, testUser.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Subscribe to non-existent author")
        void subscribeToNonExistentAuthor() throws Exception {
                mockMvc.perform(post(USER_SUBSCRIBE_AUTHOR_URL, 999)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unsubscribe from author success")
        void unsubscribeFromAuthor() throws Exception {
                UserEntity author = new UserEntity("author", "author@example.com", "password", UserRole.AUTHOR);
                author.setBlocked(false);
                author = userRepository.save(author);
                testUser.getSubscribedAuthors().add(author);
                userRepository.save(testUser);

                mockMvc.perform(delete(USER_UNSUBSCRIBE_AUTHOR_URL, author.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authors", hasSize(0)));
        }

        @Test
        @DisplayName("Get my subscriptions success")
        void getMySubscriptionsSuccess() throws Exception {
                CategoryEntity category = new CategoryEntity("Technology");
                category = categoryRepository.save(category);
                testUser.getSubscribedCategories().add(category);
                userRepository.save(testUser);

                UserEntity author = new UserEntity("author", "author@example.com", "password", UserRole.AUTHOR);
                author.setBlocked(false);
                author = userRepository.save(author);
                testUser.getSubscribedAuthors().add(author);
                userRepository.save(testUser);

                mockMvc.perform(get(USER_SUBSCRIPTIONS_URL)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(1)))
                                .andExpect(jsonPath("$.authors", hasSize(1)));
        }

        @Test
        @DisplayName("Get my subscriptions empty")
        void getMySubscriptionsEmpty() throws Exception {
                mockMvc.perform(get(USER_SUBSCRIPTIONS_URL)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(0)))
                                .andExpect(jsonPath("$.authors", hasSize(0)));
        }

        @Test
        @DisplayName("Subscribe to category without authentication")
        void subscribeWithoutAuthentication() throws Exception {
                CategoryEntity category = new CategoryEntity("Technology");
                category = categoryRepository.save(category);

                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, category.getId()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Cannot subscribe to same category twice")
        void cannotSubscribeToSameCategoryTwice() throws Exception {
                CategoryEntity category = new CategoryEntity("Technology");
                category = categoryRepository.save(category);

                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, category.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(1)));

                mockMvc.perform(post(USER_SUBSCRIBE_CATEGORY_URL, category.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categories", hasSize(1)));
        }

        @Test
        @DisplayName("Cannot subscribe to same author twice")
        void cannotSubscribeToSameAuthorTwice() throws Exception {
                UserEntity author = new UserEntity("author", "author@example.com", "password", UserRole.AUTHOR);
                author.setBlocked(false);
                author = userRepository.save(author);

                mockMvc.perform(post(USER_SUBSCRIBE_AUTHOR_URL, author.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authors", hasSize(1)));

                mockMvc.perform(post(USER_SUBSCRIBE_AUTHOR_URL, author.getId())
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authors", hasSize(1)));
        }
}
