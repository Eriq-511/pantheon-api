package com.cms.service;

import com.cms.model.ContentEntry;
import com.cms.repository.ContentEntryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContentEntryService {
    private final ContentEntryRepository contentEntryRepository;

    public ContentEntryService(ContentEntryRepository contentEntryRepository) {
        this.contentEntryRepository = contentEntryRepository;
    }

    public List<ContentEntry> getAll() {
        return contentEntryRepository.findAll();
    }

    public Optional<ContentEntry> getById(Long id) {
        return contentEntryRepository.findById(id);
    }

    public ContentEntry create(ContentEntry entry) {
        return contentEntryRepository.save(entry);
    }

    public ContentEntry update(Long id, ContentEntry updated) {
        updated.setId(id);
        return contentEntryRepository.save(updated);
    }

    public void delete(Long id) {
        contentEntryRepository.deleteById(id);
    }
}
