package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.AuthResponseDTO;
import com.github.arsenmonets.newshub.dto.LoginDTO;
import com.github.arsenmonets.newshub.dto.RegisterRequestDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.exceptions.ResourceAlreadyExistsException;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.security.CustomUserDetails;
import com.github.arsenmonets.newshub.security.JwtUtil;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private NewsHubMapper mapper;

    @InjectMocks
    private AuthService authService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity("user", "user@test.com", "encoded", UserRole.USER);
        user.setId(1L);
    }

    @Test
    @DisplayName("Register success")
    void registerSuccess() {
        RegisterRequestDTO req = new RegisterRequestDTO("user", "user@test.com", "pass");
        when(userRepository.existsByLogin("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO userDTO = new UserDTO(1L, "user", UserRole.USER, false);
        when(mapper.toUserDTO(user)).thenReturn(userDTO);
        when(jwtUtil.generateToken("user", "USER", 1L)).thenReturn("token");

        AuthResponseDTO res = authService.register(req);

        assertNotNull(res.token());
        assertEquals("token", res.token());
        assertEquals("user", res.user().login());
        assertFalse(res.user().isBlocked());
        verify(userRepository).existsByLogin("user");
        verify(userRepository).existsByEmail("user@test.com");
        verify(userRepository).save(any());
        verify(mapper).toUserDTO(user);
    }

    @Test
    @DisplayName("Register duplicate login throws exception")
    void registerDuplicateLogin() {
        RegisterRequestDTO req = new RegisterRequestDTO("user", "newemail@test.com", "pass");
        when(userRepository.existsByLogin("user")).thenReturn(true);

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(req));
        assertEquals("Login вже зареєстрований", ex.getMessage());
    }

    @Test
    @DisplayName("Register duplicate email throws exception")
    void registerDuplicateEmail() {
        RegisterRequestDTO req = new RegisterRequestDTO("newuser", "user@test.com", "pass");
        when(userRepository.existsByLogin("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(req));
        assertEquals("Email вже зареєстрований", ex.getMessage());
    }

    @Test
    @DisplayName("Authenticate success")
    void authenticateSuccess() {
        LoginDTO req = new LoginDTO("user", "pass");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UserDTO userDTO = new UserDTO(1L, "user", UserRole.USER, false);
        when(mapper.toUserDTO(user)).thenReturn(userDTO);
        when(jwtUtil.generateToken("user", "USER", 1L)).thenReturn("token");

        AuthResponseDTO res = authService.authenticate(req);

        assertEquals("token", res.token());
        assertEquals("user", res.user().login());
        assertEquals(UserRole.USER, res.user().role());
        assertFalse(res.user().isBlocked());
        verify(authenticationManager).authenticate(any());
        verify(mapper).toUserDTO(user);
    }

    @Test
    @DisplayName("Authenticate bad credentials throws exception")
    void authenticateBadCredentials() {
        LoginDTO req = new LoginDTO("user", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(req));
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Authenticate blocked user")
    void authenticateBlockedUser() {
        UserEntity blockedUser = new UserEntity("user", "user@test.com", "encoded", UserRole.USER);
        blockedUser.setId(1L);
        blockedUser.setBlocked(true);

        LoginDTO req = new LoginDTO("user", "pass");

        CustomUserDetails userDetails = new CustomUserDetails(blockedUser);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UserDTO userDTO = new UserDTO(1L, "user", UserRole.USER, true);
        when(mapper.toUserDTO(blockedUser)).thenReturn(userDTO);
        when(jwtUtil.generateToken("user", "USER", 1L)).thenReturn("token");

        AuthResponseDTO res = authService.authenticate(req);

        assertTrue(res.user().isBlocked());
        verify(mapper).toUserDTO(blockedUser);
    }

    @Test
    @DisplayName("Authenticate and verify token generation")
    void authenticateVerifyTokenGeneration() {
        LoginDTO req = new LoginDTO("user", "pass");

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UserDTO userDTO = new UserDTO(1L, "user", UserRole.USER, false);
        when(mapper.toUserDTO(user)).thenReturn(userDTO);
        when(jwtUtil.generateToken("user", "USER", 1L)).thenReturn("jwt-token-123");

        AuthResponseDTO res = authService.authenticate(req);

        assertEquals("jwt-token-123", res.token());
        verify(jwtUtil).generateToken("user", "USER", 1L);
    }

    @Test
    @DisplayName("Register new user with not blocked status")
    void registerUserNotBlocked() {
        RegisterRequestDTO req = new RegisterRequestDTO("newuser", "newuser@test.com", "pass");
        when(userRepository.existsByLogin("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO userDTO = new UserDTO(1L, "user", UserRole.USER, false);
        when(mapper.toUserDTO(user)).thenReturn(userDTO);
        when(jwtUtil.generateToken("user", "USER", 1L)).thenReturn("token");

        AuthResponseDTO res = authService.register(req);

        assertFalse(res.user().isBlocked());
        verify(passwordEncoder).encode("pass");
    }
}