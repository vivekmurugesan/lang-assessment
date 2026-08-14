package com.langassessment.service;

import com.langassessment.dto.AuthDTO;
import com.langassessment.entity.User;
import com.langassessment.repository.UserRepository;
import com.langassessment.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailAndIsActive(email, true);
    }

    @Transactional
    public AuthDTO.LoginResponse authenticate(AuthDTO.LoginRequest request) {
        Optional<User> user = userRepository.findByEmailAndIsActive(request.getEmail(), true);

        if (user.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User foundUser = user.get();

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(foundUser.getEmail(), foundUser.getRole().name());

        return AuthDTO.LoginResponse.builder()
                .token(token)
                .userId(foundUser.getId())
                .email(foundUser.getEmail())
                .name(foundUser.getName())
                .role(foundUser.getRole().name())
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }

    @Transactional
    public User createUser(String email, String name, String password, User.UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User newUser = User.builder()
                .email(email)
                .name(name)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .build();

        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
