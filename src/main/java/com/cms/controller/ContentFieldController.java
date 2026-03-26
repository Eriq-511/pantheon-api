package com.cms.controller;

import com.cms.model.ContentField;
import com.cms.service.ContentFieldService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content-fields")
public class ContentFieldController {
    private final ContentFieldService contentFieldService;

    public ContentFieldController(ContentFieldService contentFieldService) {
        this.contentFieldService = contentFieldService;
    }

    @GetMapping
    public List<ContentField> getAll() {
        return contentFieldService.getAll();
    }

    @PostMapping
    public ContentField create(@RequestBody ContentField field) {
        return contentFieldService.create(field);
    }

    @PutMapping("/{id}")
    public ContentField update(@PathVariable Long id, @RequestBody ContentField field) {
        return contentFieldService.update(id, field);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contentFieldService.delete(id);
    }
}
