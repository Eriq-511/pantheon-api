package com.cms.service;

import com.cms.model.ContentField;
import com.cms.repository.ContentFieldRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContentFieldService {
    private final ContentFieldRepository contentFieldRepository;

    public ContentFieldService(ContentFieldRepository contentFieldRepository) {
        this.contentFieldRepository = contentFieldRepository;
    }

    public List<ContentField> getAll() {
        return contentFieldRepository.findAll();
    }

    public ContentField create(ContentField field) {
        return contentFieldRepository.save(field);
    }

    public ContentField update(Long id, ContentField updated) {
        updated.setId(id);
        return contentFieldRepository.save(updated);
    }

    public void delete(Long id) {
        contentFieldRepository.deleteById(id);
    }
}
