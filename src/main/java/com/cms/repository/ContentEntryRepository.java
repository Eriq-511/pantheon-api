package com.cms.repository;

import com.cms.model.ContentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentEntryRepository extends JpaRepository<ContentEntry, Long> {
}
