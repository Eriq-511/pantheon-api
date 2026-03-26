package com.cms.service;

import com.cms.model.ContentType;
import com.cms.repository.ContentTypeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContentTypeService {
    private final ContentTypeRepository contentTypeRepository;

    public ContentTypeService(ContentTypeRepository contentTypeRepository) {
        this.contentTypeRepository = contentTypeRepository;
    }

    public List<ContentType> getAll() {
        return contentTypeRepository.findAll();
    }

    public Optional<ContentType> getById(Long id) {
        return contentTypeRepository.findById(id);
    }

    public ContentType create(ContentType type) {
        return contentTypeRepository.save(type);
    }

    public ContentType update(Long id, ContentType updated) {
        updated.setId(id);
        return contentTypeRepository.save(updated);
    }

    public void delete(Long id) {
        contentTypeRepository.deleteById(id);
    }
}
