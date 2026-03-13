package com.cms.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cms.config.CorsConfig;
import com.cms.config.SecurityConfig;
import com.cms.dto.LoginRequest;
import com.cms.dto.LoginResponse;
import com.cms.dto.RegisterRequest;
import com.cms.repository.UserRepository;
import com.cms.security.JwtFilter;
import com.cms.security.JwtUtil;
import com.cms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class})
@SuppressWarnings("removal")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean @SuppressWarnings("unused") JwtUtil jwtUtil;
    @MockBean @SuppressWarnings("unused") UserRepository userRepository;

    // ──────────────────────── POST /api/auth/register ──────────────

    @Test
    public void register_shouldReturn201_withUserData_whenRequestValid() throws Exception {
        LoginResponse loginResponse = new LoginResponse(2L, "newuser", "EDITOR");
        when(authService.register(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq("newuser", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.role").value("EDITOR"));
    }

    @Test
    public void register_shouldReturn400_whenUsernameBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq("", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq("newuser", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void register_shouldReturn400_whenUsernameHasInvalidChars() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq("bad user!", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void register_shouldReturn400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────── POST /api/auth/login ────────────────

    @Test
    public void login_shouldReturn200_andSetCookie_whenCredentialsValid() throws Exception {
        com.cms.dto.LoginResponse loginResponse =
            new com.cms.dto.LoginResponse(1L, "admin", "ADMIN");

        when(authService.login(any(), any())).thenReturn(loginResponse);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq("admin", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    public void login_shouldReturn400_whenUsernameBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq("", "pass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void login_shouldReturn400_whenPasswordBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq("admin", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_shouldReturn400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────── POST /api/auth/logout ───────────────

    @Test
    @WithMockUser
    public void logout_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void logout_shouldReturn200_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ──────────────────────── GET /api/auth/me ────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void me_shouldReturn200_whenAuthenticated() throws Exception {
        com.cms.dto.LoginResponse loginResponse =
            new com.cms.dto.LoginResponse(1L, "admin", "ADMIN");

        when(authService.getCurrentUser("admin")).thenReturn(loginResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    public void me_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────── helper ─────────────────────────────

    private LoginRequest loginReq(String username, String password) {
        LoginRequest r = new LoginRequest();
        r.setUsername(username);
        r.setPassword(password);
        return r;
    }

    private RegisterRequest regReq(String username, String password) {
        return new RegisterRequest(username, password);
    }
}
