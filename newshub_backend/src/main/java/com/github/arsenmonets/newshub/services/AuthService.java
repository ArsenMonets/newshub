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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final NewsHubMapper mapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtUtil jwtUtil, NewsHubMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
    }

    public AuthResponseDTO register(RegisterRequestDTO data) {
        if (userRepository.existsByLogin(data.login())) {
            throw new ResourceAlreadyExistsException("Login вже зареєстрований");
        }
        if (userRepository.existsByEmail(data.email())) {
            throw new ResourceAlreadyExistsException("Email вже зареєстрований");
        }
        UserEntity newUser = new UserEntity(data.login(), data.email(),
                passwordEncoder.encode(data.password()), UserRole.USER);
        newUser.setBlocked(false);
        UserEntity user = userRepository.save(newUser);
        return buildResponse(mapper.toUserDTO(user));
    }

    public AuthResponseDTO authenticate(LoginDTO credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUserEntity();
        return buildResponse(mapper.toUserDTO(user));
    }

    private AuthResponseDTO buildResponse(UserDTO dto) {
        String token = jwtUtil.generateToken(dto.login(), dto.role().name(), dto.id());
        return new AuthResponseDTO(token, dto);
    }
}
