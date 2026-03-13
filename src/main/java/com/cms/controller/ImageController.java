package com.cms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cms.dto.ApiResponse;
import com.cms.dto.ImageResponse;
import com.cms.dto.ImageUpdateRequest;
import com.cms.service.ImageService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/images")
@Validated
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageResponse>>> getAllImages() {
        return ResponseEntity.ok(ApiResponse.ok(imageService.getAllImages()));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ImageResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false)
            @Size(max = 255, message = "Alt text must be at most 255 characters")
            String altText) {
        ImageResponse uploaded = imageService.uploadImage(file, altText);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(uploaded, "Image uploaded successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageResponse>> updateImage(
            @PathVariable @Positive(message = "Id must be a positive number") Long id,
            @Valid @RequestBody ImageUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(imageService.updateImage(id, request), "Image updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable @Positive(message = "Id must be a positive number") Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Image deleted successfully"));
    }
}
