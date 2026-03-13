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
import com.cms.dto.MenuItemRequest;
import com.cms.dto.MenuItemResponse;
import com.cms.dto.ReorderRequest;
import com.cms.service.MenuService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/menu")
@Validated
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAllMenuItems() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllMenuItems()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse created = menuService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Menu item created successfully"));
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderMenuItems(
            @Valid @RequestBody ReorderRequest request) {
        menuService.reorderMenuItems(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Menu reordered successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable @Positive(message = "Id must be a positive number") Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(menuService.updateMenuItem(id, request), "Menu item updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable @Positive(message = "Id must be a positive number") Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Menu item deleted successfully"));
    }
}
