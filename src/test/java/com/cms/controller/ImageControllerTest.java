package com.cms.controller;

import com.cms.config.CorsConfig;
import com.cms.config.SecurityConfig;
import com.cms.dto.ImageResponse;
import com.cms.dto.ImageUpdateRequest;
import com.cms.repository.UserRepository;
import com.cms.security.JwtFilter;
import com.cms.security.JwtUtil;
import com.cms.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class})
@SuppressWarnings("removal")
class ImageControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ImageService imageService;
        @MockBean JwtUtil jwtUtil;
        @MockBean UserRepository userRepository;

    private final ImageResponse sample = new ImageResponse(
            1L,
            "photo.jpg",
            "https://res.cloudinary.com/test/photo.jpg",
            "pantheon-cms/photo",
            "A photo",
            LocalDateTime.now()
    );

    // ──────────────────────── GET /api/images  (auth required) ───

    @Test
    @WithMockUser(roles = "ADMIN")
        public void getAllImages_shouldReturn200_whenAuthenticated() throws Exception {
                Objects.requireNonNull(jwtUtil);
                Objects.requireNonNull(userRepository);
        when(imageService.getAllImages()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].filename").value("photo.jpg"));
    }

    @Test
        public void getAllImages_shouldReturn401_whenNotAuthenticated() throws Exception {
                Objects.requireNonNull(jwtUtil);
                Objects.requireNonNull(userRepository);
        mockMvc.perform(get("/api/images"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────── POST /api/images/upload ────────────

    @Test
    @WithMockUser(roles = "ADMIN")
        public void uploadImage_shouldReturn201_whenAuthenticatedAndValidFile() throws Exception {
        when(imageService.uploadImage(any(), any())).thenReturn(sample);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "jpeg-bytes".getBytes());

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("altText", "A photo"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.filename").value("photo.jpg"));
    }

    @Test
        public void uploadImage_shouldReturn401_whenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "bytes".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
        public void uploadImage_shouldReturn400_whenServiceThrowsBadRequest() throws Exception {
        when(imageService.uploadImage(any(), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "File is empty"));

        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/images/upload").file(empty))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void uploadImage_shouldReturn400_whenAltTextTooLong() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "jpeg-bytes".getBytes());

        String tooLongAlt = "a".repeat(256);

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("altText", tooLongAlt))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    // ──────────────────────── PUT /api/images/{id} ───────────────

    @Test
    @WithMockUser(roles = "ADMIN")
        public void updateImage_shouldReturn200_whenAuthenticatedAndFound() throws Exception {
        ImageResponse updated = new ImageResponse(
                1L,
                "photo.jpg",
                "https://res.cloudinary.com/test/photo.jpg",
                "pantheon-cms/photo",
                "Updated alt",
                LocalDateTime.now()
        );
        when(imageService.updateImage(eq(1L), any())).thenReturn(updated);

        ImageUpdateRequest req = new ImageUpdateRequest();
        req.setAltText("Updated alt");

        mockMvc.perform(put("/api/images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.altText").value("Updated alt"));
    }

    @Test
        public void updateImage_shouldReturn401_whenNotAuthenticated() throws Exception {
        ImageUpdateRequest req = new ImageUpdateRequest();
        req.setAltText("alt");

        mockMvc.perform(put("/api/images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
        public void updateImage_shouldReturn404_whenNotFound() throws Exception {
        when(imageService.updateImage(eq(99L), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Image not found"));

        ImageUpdateRequest req = new ImageUpdateRequest();
        req.setAltText("alt");

        mockMvc.perform(put("/api/images/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void updateImage_shouldReturn400_whenIdNonPositive() throws Exception {
                ImageUpdateRequest req = new ImageUpdateRequest();
                req.setAltText("alt");

                mockMvc.perform(put("/api/images/0")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void updateImage_shouldReturn400_whenAltTextTooLong() throws Exception {
                ImageUpdateRequest req = new ImageUpdateRequest();
                req.setAltText("a".repeat(256));

                mockMvc.perform(put("/api/images/1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.status").value(400));
        }

    // ──────────────────────── DELETE /api/images/{id} ────────────

    @Test
    @WithMockUser(roles = "ADMIN")
        public void deleteImage_shouldReturn200_whenFound() throws Exception {
        doNothing().when(imageService).deleteImage(1L);

        mockMvc.perform(delete("/api/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
        public void deleteImage_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/images/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
        public void deleteImage_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Image not found"))
                .when(imageService).deleteImage(99L);

        mockMvc.perform(delete("/api/images/99"))
                .andExpect(status().isNotFound());
    }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void deleteImage_shouldReturn400_whenIdNonPositive() throws Exception {
                mockMvc.perform(delete("/api/images/0"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.status").value(400));
        }
}
