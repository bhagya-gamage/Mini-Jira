package com.minijira.service;

import com.minijira.dto.AuthDtos.*;
import com.minijira.exception.BadRequestException;
import com.minijira.model.Role;
import com.minijira.model.User;
import com.minijira.repository.UserRepository;
import com.minijira.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthResponse register(RegisterRequest r) {
        if (users.existsByEmail(r.email())) throw new BadRequestException("Email already registered");
        if (r.role() == Role.ADMIN) throw new BadRequestException("Cannot self-register as ADMIN");
        User u = new User();
        u.setFullName(r.fullName());
        u.setEmail(r.email().toLowerCase());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(r.role() == null ? Role.DEVELOPER : r.role());
        return tokens(users.save(u));
    }

    public AuthResponse login(LoginRequest r) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(r.email().toLowerCase(), r.password()));
        return tokens(users.findByEmail(r.email().toLowerCase()).orElseThrow());
    }

    public AuthResponse refresh(RefreshRequest r) {
        try {
            Claims c = jwt.parse(r.refreshToken());
            if (!"refresh".equals(c.get("type", String.class))) throw new BadCredentialsException("Not a refresh token");
            return tokens(users.findByEmail(c.getSubject()).orElseThrow(() -> new BadCredentialsException("User gone")));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private AuthResponse tokens(User u) {
        return new AuthResponse(jwt.generateAccessToken(u), jwt.generateRefreshToken(u), "Bearer", UserDto.from(u));
    }
}
