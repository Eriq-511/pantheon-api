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
import org.springframework.web.bind.annotation.RestController;

import com.cms.dto.ApiResponse;
import com.cms.dto.PageRequest;
import com.cms.dto.PageResponse;
import com.cms.service.PageService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/pages")
@Validated
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PageResponse>>> getAllPages(
            org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(ApiResponse.ok(pageService.getAllPages(isAdmin)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<PageResponse>> getPageById(
            @PathVariable @Positive(message = "Id must be a positive number") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pageService.getPageById(id)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PageResponse>> getPageBySlug(
            @PathVariable
            @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase letters, numbers, and hyphens only")
            String slug) {
        return ResponseEntity.ok(ApiResponse.ok(pageService.getPageBySlug(slug)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse>> createPage(
            @Valid @RequestBody PageRequest request) {
        PageResponse created = pageService.createPage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Page created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(
            @PathVariable @Positive(message = "Id must be a positive number") Long id,
            @Valid @RequestBody PageRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(pageService.updatePage(id, request), "Page updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePage(
            @PathVariable @Positive(message = "Id must be a positive number") Long id) {
        pageService.deletePage(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Page deleted successfully"));
    }

    // Debug endpoint to list all slugs
    @GetMapping("/debug-slugs")
    public ResponseEntity<ApiResponse<List<String>>> getAllSlugs() {
        List<String> slugs = pageService.getAllSlugs();
        return ResponseEntity.ok(ApiResponse.ok(slugs));
    }
}
