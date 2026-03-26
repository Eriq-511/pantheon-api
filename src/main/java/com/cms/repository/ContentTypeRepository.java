package com.cms.repository;

import com.cms.model.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentTypeRepository extends JpaRepository<ContentType, Long> {
    Optional<ContentType> findBySlug(String slug);
}
