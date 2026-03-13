package com.cms.controller;

import com.cms.config.CorsConfig;
import com.cms.config.SecurityConfig;
import com.cms.dto.MenuItemRequest;
import com.cms.dto.MenuItemResponse;
import com.cms.dto.ReorderRequest;
import com.cms.repository.UserRepository;
import com.cms.security.JwtFilter;
import com.cms.security.JwtUtil;
import com.cms.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class})
@SuppressWarnings("removal")
class MenuControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean MenuService menuService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserRepository userRepository;

    private final MenuItemResponse sample = new MenuItemResponse(
            1L,
            "Home",
            "/",
            null,
            0,
            null,
            null
        );

    // ──────────────────────── GET /api/menu  (public) ────────────

    @Test
    public void getAllMenuItems_shouldReturn200_withoutAuth() throws Exception {
        Objects.requireNonNull(jwtUtil);
        Objects.requireNonNull(userRepository);
        when(menuService.getAllMenuItems()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].label").value("Home"));
    }

    @Test
    public void getAllMenuItems_shouldReturnEmptyArray_whenNoItems() throws Exception {
        Objects.requireNonNull(jwtUtil);
        Objects.requireNonNull(userRepository);
        when(menuService.getAllMenuItems()).thenReturn(List.of());

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ──────────────────────── POST /api/menu  (auth required) ────

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createMenuItem_shouldReturn201_whenAuthenticatedAndValid() throws Exception {
        when(menuService.createMenuItem(any())).thenReturn(sample);

        mockMvc.perform(post("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("Home", "/"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.label").value("Home"));
    }

    @Test
    public void createMenuItem_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("Home", "/"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createMenuItem_shouldReturn400_whenLabelBlank() throws Exception {
        mockMvc.perform(post("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("", "/"))))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────── PUT /api/menu/reorder ──────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    public void reorderMenuItems_shouldReturn200() throws Exception {
        doNothing().when(menuService).reorderMenuItems(any());

        ReorderRequest.ReorderItem item = new ReorderRequest.ReorderItem();
        item.setId(1L);
        item.setOrderIndex(0);

        ReorderRequest reorderReq = new ReorderRequest();
        reorderReq.setItems(List.of(item));

        mockMvc.perform(put("/api/menu/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void reorderMenuItems_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/menu/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void reorderMenuItems_shouldReturn400_whenItemsEmpty() throws Exception {
        ReorderRequest reorderReq = new ReorderRequest();
        reorderReq.setItems(List.of());

        mockMvc.perform(put("/api/menu/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(menuService, never()).reorderMenuItems(any());
    }

    // ──────────────────────── PUT /api/menu/{id} ─────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateMenuItem_shouldReturn200() throws Exception {
        when(menuService.updateMenuItem(eq(1L), any())).thenReturn(sample);

        mockMvc.perform(put("/api/menu/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("Home v2", "/"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.label").value("Home"));
    }

    @Test
    public void updateMenuItem_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/menu/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("X", "/"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateMenuItem_shouldReturn400_whenIdNonPositive() throws Exception {
        mockMvc.perform(put("/api/menu/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("Home", "/"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(menuService, never()).updateMenuItem(any(), any());
    }

    // ──────────────────────── DELETE /api/menu/{id} ──────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteMenuItem_shouldReturn200_whenFound() throws Exception {
        doNothing().when(menuService).deleteMenuItem(1L);

        mockMvc.perform(delete("/api/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void deleteMenuItem_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/menu/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteMenuItem_shouldReturn400_whenIdNonPositive() throws Exception {
        mockMvc.perform(delete("/api/menu/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(menuService, never()).deleteMenuItem(any());
    }

    // ──────────────────────── helper ─────────────────────────────

    private MenuItemRequest req(String label, String url) {
        MenuItemRequest r = new MenuItemRequest();
        r.setLabel(label);
        r.setUrl(url);
        return r;
    }
}
