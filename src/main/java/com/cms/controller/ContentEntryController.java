package com.cms.controller;

import com.cms.model.ContentEntry;
import com.cms.service.ContentEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content-entries")
public class ContentEntryController {
    private final ContentEntryService contentEntryService;

    public ContentEntryController(ContentEntryService contentEntryService) {
        this.contentEntryService = contentEntryService;
    }

    @GetMapping
    public List<ContentEntry> getAll() {
        return contentEntryService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentEntry> getById(@PathVariable Long id) {
        return contentEntryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ContentEntry create(@RequestBody ContentEntry entry) {
        return contentEntryService.create(entry);
    }

    @PutMapping("/{id}")
    public ContentEntry update(@PathVariable Long id, @RequestBody ContentEntry entry) {
        return contentEntryService.update(id, entry);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contentEntryService.delete(id);
    }
}
