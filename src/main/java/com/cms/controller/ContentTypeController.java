package com.cms.controller;

import com.cms.model.ContentType;
import com.cms.service.ContentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content-types")
public class ContentTypeController {
    private final ContentTypeService contentTypeService;

    public ContentTypeController(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    @GetMapping
    public List<ContentType> getAll() {
        return contentTypeService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentType> getById(@PathVariable Long id) {
        return contentTypeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ContentType create(@RequestBody ContentType type) {
        return contentTypeService.create(type);
    }

    @PutMapping("/{id}")
    public ContentType update(@PathVariable Long id, @RequestBody ContentType type) {
        return contentTypeService.update(id, type);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contentTypeService.delete(id);
    }
}
