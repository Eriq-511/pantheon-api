package com.cms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cms.model.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
    java.util.List<Page> findByStatus(String status);
}
