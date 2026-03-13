package com.cms.controller;

import com.cms.dto.ApiResponse;
import com.cms.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    /** Public — used by the public site (header, footer). */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok(settingService.getAll()));
    }

    /** Authenticated — only admin/editor can update site settings. */
    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSettings(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(settingService.saveAll(body)));
    }
}
