package com.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.cms.AbstractIntegrationTest;
import com.cms.dto.LoginRequest;
import com.cms.dto.LoginResponse;
import com.cms.dto.RegisterRequest;
import com.cms.model.User;
import com.cms.repository.UserRepository;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    //  register 

    @Test
    public void register_shouldCreateAdminUser_andReturnLoginResponse() {
        LoginResponse result = authService.register(new RegisterRequest("newadmin", "securepass1"));

        assertThat(result.getUsername()).isEqualTo("newadmin");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getId()).isNotNull();

        User saved = userRepository.findByUsername("newadmin").orElseThrow();
        assertThat(passwordEncoder.matches("securepass1", saved.getPasswordHash())).isTrue();
    }

    @Test
    public void register_shouldThrow409_whenUsernameAlreadyTaken() {
        seedAdminUser();
        assertThatThrownBy(() -> authService.register(new RegisterRequest("admin", "anotherpass")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Username is already taken");
    }

    @Test
    public void register_shouldNotIssueJwtCookie() {
        // register() returns a LoginResponse but must NOT set any cookie
        // (user is required to login separately)
        LoginResponse result = authService.register(new RegisterRequest("cookieless", "password99"));
        assertThat(result.getUsername()).isEqualTo("cookieless");
        // No HttpServletResponse is passed — confirms the method signature does not accept one
    }

    //  login 

    @Test
    public void login_shouldReturnLoginResponse_andSetJwtCookie_whenCredentialsValid() {
        seedAdminUser();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginResponse result = authService.login(req("admin", "secret"), response);

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getId()).isNotNull();

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("jwt=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=None");
        assertThat(setCookie).doesNotContain("Max-Age=0");
    }

    @Test
    public void login_shouldThrow401_whenUserNotFound() {
        assertThatThrownBy(() ->
                authService.login(req("unknown", "any"), new MockHttpServletResponse()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    public void login_shouldThrow401_whenPasswordDoesNotMatch() {
        seedAdminUser();
        assertThatThrownBy(() ->
                authService.login(req("admin", "wrong-password"), new MockHttpServletResponse()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid username or password");
    }

    //  logout 

    @Test
    public void logout_shouldSetExpiredCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        authService.logout(response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("jwt=");
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
    }

    //  getCurrentUser 

    @Test
    public void getCurrentUser_shouldReturnUserDetails_whenUserExists() {
        seedAdminUser();
        LoginResponse result = authService.getCurrentUser("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void getCurrentUser_shouldThrow401_whenUserNotFound() {
        assertThatThrownBy(() -> authService.getCurrentUser("ghost"))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  helper 

    private LoginRequest req(String username, String password) {
        LoginRequest r = new LoginRequest();
        r.setUsername(username);
        r.setPassword(password);
        return r;
    }

    private void seedAdminUser() {
        // Insert a test user with a real BCrypt-encoded password.
        User user = new User();
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("secret"));
        user.setRole("ADMIN");
        userRepository.save(user);
    }
}
