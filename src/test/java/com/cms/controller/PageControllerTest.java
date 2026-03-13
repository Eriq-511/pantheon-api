package com.cms.controller;

import com.cms.config.CorsConfig;
import com.cms.config.SecurityConfig;
import com.cms.dto.PageRequest;
import com.cms.dto.PageResponse;
import com.cms.repository.UserRepository;
import com.cms.security.JwtFilter;
import com.cms.security.JwtUtil;
import com.cms.service.PageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class})
@SuppressWarnings("removal")
class PageControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PageService pageService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserRepository userRepository;

    private final PageResponse sampleResponse = samplePageResponse();

    // ──────────────────────── GET /api/pages  (public) ───────────

    @Test
    public void getAllPages_shouldReturn200_withoutAuth() throws Exception {
        Objects.requireNonNull(jwtUtil);
        Objects.requireNonNull(userRepository);
        when(pageService.getAllPages(false)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("home"));
    }

    @Test
    public void getAllPages_shouldReturnEmptyList_whenNoPagesExist() throws Exception {
        Objects.requireNonNull(jwtUtil);
        Objects.requireNonNull(userRepository);
        when(pageService.getAllPages(false)).thenReturn(List.of());

        mockMvc.perform(get("/api/pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ──────────────────────── GET /api/pages/{slug}  (public) ────

    @Test
    public void getPageBySlug_shouldReturn200_whenFound() throws Exception {
        when(pageService.getPageBySlug("home")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/pages/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Home"));
    }

    @Test
    public void getPageBySlug_shouldReturn404_whenNotFound() throws Exception {
        when(pageService.getPageBySlug("missing"))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Page not found with slug: missing"
                ));

        mockMvc.perform(get("/api/pages/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPageBySlug_shouldReturn400_whenSlugInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/pages/INVALID SLUG!!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(pageService, never()).getPageBySlug(any());
    }

    // ──────────────────────── POST /api/pages  (auth required) ───

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPage_shouldReturn201_whenAuthenticated() throws Exception {
        when(pageService.createPage(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("Home", "home", "<p>Hi</p>", "published"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").value("home"));
    }

    @Test
    public void createPage_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("Home", "home", "", "draft"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPage_shouldReturn400_whenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("", "home", "", "draft"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPage_shouldReturn400_whenSlugInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("Title", "INVALID SLUG!!", "", "draft"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPage_shouldReturn409_whenSlugAlreadyExists() throws Exception {
        when(pageService.createPage(any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "already exists"
                ));

        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("Home", "home", "", "draft"))))
                .andExpect(status().isConflict());
    }

    // ──────────────────────── PUT /api/pages/{id}  (auth required)

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatePage_shouldReturn200_whenAuthenticatedAndValid() throws Exception {
        PageResponse updated = new PageResponse(
                1L,
                "Updated",
                "home",
                null,
                "published",
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(pageService.updatePage(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/pages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("Updated", "home", "", "published"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated"));
    }

    @Test
    public void updatePage_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/pages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("X", "x", "", "draft"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatePage_shouldReturn400_whenIdNonPositive() throws Exception {
        mockMvc.perform(put("/api/pages/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                pageReq("X", "x", "", "draft"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(pageService, never()).updatePage(anyLong(), any());
    }

    // ──────────────────────── DELETE /api/pages/{id} ─────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deletePage_shouldReturn200_whenAuthenticatedAndFound() throws Exception {
        doNothing().when(pageService).deletePage(1L);

        mockMvc.perform(delete("/api/pages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void deletePage_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/pages/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deletePage_shouldReturn400_whenIdNonPositive() throws Exception {
        mockMvc.perform(delete("/api/pages/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        verify(pageService, never()).deletePage(anyLong());
    }

    // ──────────────────────── helper ─────────────────────────────

    private PageRequest pageReq(String title, String slug, String content, String status) {
        PageRequest r = new PageRequest();
        r.setTitle(title);
        r.setSlug(slug);
        r.setContent(content);
        r.setStatus(status);
        return r;
    }

    private static PageResponse samplePageResponse() {
        return new PageResponse(
                1L,
                "Home",
                "home",
                "<p>Hi</p>",
                "published",
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
