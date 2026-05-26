package com.github.arsenmonets.newshub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arsenmonets.newshub.dto.NewsInputDTO;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.NewsRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("AuthorController Integration Tests")
@TestPropertySource(locations = "classpath:application-integration.properties")
class AuthorControllerIntegrationTests {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private NewsRepository newsRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtUtil jwtUtil;

        private String authorToken;
        private String userToken;
        private CategoryEntity testCategory;

        private String otherAuthorToken;

        private static final String AUTHOR_CREATE_NEWS_URL = "/api/v1/author/news";
        private static final String AUTHOR_UPDATE_NEWS_URL = "/api/v1/author/news/{id}";
        private static final String AUTHOR_DELETE_NEWS_URL = "/api/v1/author/news/{id}";

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();

                categoryRepository.deleteAll();
                newsRepository.deleteAll();

                UserEntity author = new UserEntity("author", "author@example.com", "password", UserRole.AUTHOR);
                author.setBlocked(false);
                author = userRepository.save(author);
                authorToken = jwtUtil.generateToken(author.getLogin(), author.getRole().name(), author.getId());

                UserEntity testUser = new UserEntity("testuser", "user@example.com", "password", UserRole.USER);
                testUser.setBlocked(false);
                testUser = userRepository.save(testUser);
                userToken = jwtUtil.generateToken(testUser.getLogin(), testUser.getRole().name(), testUser.getId());

                UserEntity otherAuthor = new UserEntity("otherauthor", "otherauthor@example.com", "password",
                                UserRole.AUTHOR);
                otherAuthor.setBlocked(false);
                otherAuthor = userRepository.save(otherAuthor);
                otherAuthorToken = jwtUtil.generateToken(otherAuthor.getLogin(), otherAuthor.getRole().name(),
                                otherAuthor.getId());

                testCategory = new CategoryEntity("Technology");
                testCategory = categoryRepository.save(testCategory);
        }

        @Test
        @DisplayName("Create news success")
        void testCreateNewsSuccess() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());

                mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.title").value("Test News"))
                                .andExpect(jsonPath("$.content").value("Test Content"));
        }

        @Test
        @DisplayName("Create news without authentication")
        void testCreateNewsWithoutAuthentication() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());

                mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Create news with non-existent category")
        void testCreateNewsWithNonExistentCategory() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", 999L);

                mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Create news as user role fails")
        void testCreateNewsAsUserRoleFails() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());

                mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Update news success")
        void testUpdateNewsSuccess() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Original Title", "Original Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                NewsInputDTO updateInput = new NewsInputDTO("Updated Title", "Updated Content", testCategory.getId());

                mockMvc.perform(put(AUTHOR_UPDATE_NEWS_URL, newsId)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateInput)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Title"))
                                .andExpect(jsonPath("$.content").value("Updated Content"));
        }

        @Test
        @DisplayName("Update news not found")
        void testUpdateNewsNotFound() throws Exception {
                NewsInputDTO updateInput = new NewsInputDTO("Updated Title", "Updated Content", testCategory.getId());

                mockMvc.perform(put(AUTHOR_UPDATE_NEWS_URL, 999)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateInput)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Update news not owned by author")
        void testUpdateNewsNotOwnedByAuthor() throws Exception {

                NewsInputDTO newsInput = new NewsInputDTO("Original Title", "Original Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + otherAuthorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                NewsInputDTO updateInput = new NewsInputDTO("Updated Title", "Updated Content", testCategory.getId());

                mockMvc.perform(put(AUTHOR_UPDATE_NEWS_URL, newsId)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateInput)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Update news without authentication")
        void testUpdateNewsWithoutAuthentication() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Original Title", "Original Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                NewsInputDTO updateInput = new NewsInputDTO("Updated Title", "Updated Content", testCategory.getId());

                mockMvc.perform(put(AUTHOR_UPDATE_NEWS_URL, newsId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateInput)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Delete news success")
        void testDeleteNewsSuccess() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                mockMvc.perform(delete(AUTHOR_DELETE_NEWS_URL, newsId)
                                .header("Authorization", "Bearer " + authorToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Delete news not found")
        void testDeleteNewsNotFound() throws Exception {
                mockMvc.perform(delete(AUTHOR_DELETE_NEWS_URL, 999)
                                .header("Authorization", "Bearer " + authorToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Delete news not owned by author")
        void testDeleteNewsNotOwnedByAuthor() throws Exception {

                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + otherAuthorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                mockMvc.perform(delete(AUTHOR_DELETE_NEWS_URL, newsId)
                                .header("Authorization", "Bearer " + authorToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Delete news without authentication")
        void testDeleteNewsWithoutAuthentication() throws Exception {
                NewsInputDTO newsInput = new NewsInputDTO("Test News", "Test Content", testCategory.getId());
                String createResponse = mockMvc.perform(post(AUTHOR_CREATE_NEWS_URL)
                                .header("Authorization", "Bearer " + authorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newsInput)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long newsId = objectMapper.readTree(createResponse).get("id").asLong();

                mockMvc.perform(delete(AUTHOR_DELETE_NEWS_URL, newsId))
                                .andExpect(status().isUnauthorized());
        }
}