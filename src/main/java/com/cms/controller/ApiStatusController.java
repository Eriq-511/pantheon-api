package com.cms.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiStatusController {
    @GetMapping("")
    public Map<String, String> apiStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "Pantheon API is running.");
        return status;
    }
}
