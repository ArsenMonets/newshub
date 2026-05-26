package com.github.arsenmonets.newshub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arsenmonets.newshub.dto.LoginDTO;
import com.github.arsenmonets.newshub.dto.RegisterRequestDTO;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("AuthController Integration Tests")
@TestPropertySource(locations = "classpath:application-integration.properties")
class AuthControllerIntegrationTests {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private static final String AUTH_REGISTER_URL = "/api/v1/auth/register";
        private static final String AUTH_LOGIN_URL = "/api/v1/auth/login";

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("Register success with valid credentials")
        void registerSuccessWithValidCredentials() throws Exception {
                RegisterRequestDTO request = new RegisterRequestDTO("testuser", "test@example.com", "password123");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token", notNullValue()))
                                .andExpect(jsonPath("$.user.login").value("testuser"))
                                .andExpect(jsonPath("$.user.id", notNullValue()))
                                .andExpect(jsonPath("$.user.role").value("USER"))
                                .andExpect(jsonPath("$.user.isBlocked").value(false));
        }

        @Test
        @DisplayName("Register fails with duplicate login")
        void registerFailsWithDuplicateLogin() throws Exception {
                UserEntity existingUser = new UserEntity("duplicateuser", "existing@example.com",
                                passwordEncoder.encode("password123"), UserRole.USER);
                existingUser.setBlocked(false);
                userRepository.save(existingUser);

                RegisterRequestDTO request = new RegisterRequestDTO("duplicateuser", "new@example.com", "password123");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Register fails with duplicate email")
        void registerFailsWithDuplicateEmail() throws Exception {
                UserEntity existingUser = new UserEntity("existinguser", "duplicate@example.com",
                                passwordEncoder.encode("password123"), UserRole.USER);
                existingUser.setBlocked(false);
                userRepository.save(existingUser);

                RegisterRequestDTO request = new RegisterRequestDTO("newuser", "duplicate@example.com", "password123");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Register fails with invalid email format")
        void registerFailsWithInvalidEmailFormat() throws Exception {
                RegisterRequestDTO request = new RegisterRequestDTO("testuser", "invalidemail", "password123");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register fails with short password")
        void registerFailsWithShortPassword() throws Exception {
                RegisterRequestDTO request = new RegisterRequestDTO("testuser", "test@example.com", "pass");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register fails with empty login")
        void registerFailsWithEmptyLogin() throws Exception {
                RegisterRequestDTO request = new RegisterRequestDTO("", "test@example.com", "password123");

                mockMvc.perform(post(AUTH_REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login success with valid credentials")
        void loginSuccessWithValidCredentials() throws Exception {
                UserEntity user = new UserEntity("testuser", "test@example.com",
                                passwordEncoder.encode("password123"), UserRole.USER);
                user.setBlocked(false);
                userRepository.save(user);

                LoginDTO request = new LoginDTO("testuser", "password123");

                mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token", notNullValue()))
                                .andExpect(jsonPath("$.user.login").value("testuser"))
                                .andExpect(jsonPath("$.user.role").value("USER"));
        }

        @Test
        @DisplayName("Login fails with invalid password")
        void loginFailsWithInvalidPassword() throws Exception {
                UserEntity user = new UserEntity("testuser", "test@example.com",
                                passwordEncoder.encode("password123"), UserRole.USER);
                user.setBlocked(false);
                userRepository.save(user);

                LoginDTO request = new LoginDTO("testuser", "wrongpassword");

                mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Login fails with non-existent user")
        void loginFailsWithNonExistentUser() throws Exception {
                LoginDTO request = new LoginDTO("nonexistent", "password123");

                mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Login fails with empty login")
        void loginFailsWithEmptyLogin() throws Exception {
                LoginDTO request = new LoginDTO("", "password123");

                mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login fails with empty password")
        void loginFailsWithEmptyPassword() throws Exception {
                LoginDTO request = new LoginDTO("testuser", "");

                mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}
