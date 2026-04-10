package com.formulamatch.service;

import com.formulamatch.dto.AccountDto;
import com.formulamatch.dto.AuthResponse;
import com.formulamatch.dto.LoginRequest;
import com.formulamatch.dto.RegisterRequest;
import com.formulamatch.entity.User;
import com.formulamatch.exception.EmailAlreadyExistsException;
import com.formulamatch.exception.InvalidCredentialsException;
import com.formulamatch.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setApiKey(UUID.randomUUID().toString());

        userRepository.save(user);
        return new AuthResponse(user.getApiKey());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(user.getApiKey());
    }

    public AccountDto getMe() {
        User user = currentUser();
        return new AccountDto(user.getEmail(), user.getApiKey());
    }

    @Transactional
    public void deleteAccount() {
        userRepository.delete(currentUser());
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof User user) {
            return user;
        }
        throw new InvalidCredentialsException();
    }
}
