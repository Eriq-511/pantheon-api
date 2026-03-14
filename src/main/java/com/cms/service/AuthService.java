package com.cms.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cms.dto.LoginRequest;
import com.cms.dto.LoginResponse;
import com.cms.dto.RegisterRequest;
import com.cms.model.User;
import com.cms.repository.UserRepository;
import com.cms.security.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private static final String JWT_COOKIE_NAME = "jwt";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String secureCookieFlag;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       @org.springframework.beans.factory.annotation.Value("${server.secure-cookie:false}") boolean secureCookie) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.secureCookieFlag = secureCookie ? "; Secure" : "";
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username is already taken");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole("ADMIN");
        newUser.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(newUser);

        // Do not auto-login — user must sign in explicitly after registration.
        return new LoginResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        setJwtCookie(response, token, (int) (jwtUtil.getExpirationMs() / 1000));

        return new LoginResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public void logout(HttpServletResponse response) {
        setJwtCookie(response, "", 0);
    }

    public LoginResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return new LoginResponse(user.getId(), user.getUsername(), user.getRole());
    }

    private void setJwtCookie(HttpServletResponse response, String value, int maxAge) {
        // Always set SameSite=None; Secure for cross-site cookie usage
        String cookieHeader = String.format(
            "%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=None; Secure",
            JWT_COOKIE_NAME, value, maxAge);
        // Use addHeader (not setHeader) so we never clobber other Set-Cookie headers.
        response.addHeader("Set-Cookie", cookieHeader);
    }
}
