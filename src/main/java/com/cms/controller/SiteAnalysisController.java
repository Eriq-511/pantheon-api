package com.cms.controller;

import com.cms.dto.ApiResponse;
import com.cms.dto.SiteAnalysisRequest;
import com.cms.dto.SiteAnalysisResponse;
import com.cms.service.SiteAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site-analysis")
public class SiteAnalysisController {

    private final SiteAnalysisService siteAnalysisService;

    public SiteAnalysisController(SiteAnalysisService siteAnalysisService) {
        this.siteAnalysisService = siteAnalysisService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SiteAnalysisResponse>> analyze(
            @Valid @RequestBody SiteAnalysisRequest request) {
        SiteAnalysisResponse result = siteAnalysisService.analyze(request.getUrl());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
