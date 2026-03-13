package com.cms.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cms.dto.PageRequest;
import com.cms.dto.PageResponse;
import com.cms.model.Page;
import com.cms.repository.PageRepository;
import com.cms.util.InputSanitizer;

@Service
public class PageService {

    private final PageRepository pageRepository;

    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public List<PageResponse> getAllPages(boolean includeAll) {
        List<Page> pages = includeAll
                ? pageRepository.findAll()
                : pageRepository.findByStatus("published");
        return pages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PageResponse getPageBySlug(String slug) {
        Page page = pageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Page not found with slug: " + slug));
        return toResponse(page);
    }

    public PageResponse getPageById(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Page not found with id: " + id));
        return toResponse(page);
    }

    @Transactional
    public PageResponse createPage(PageRequest request) {
        String slug = InputSanitizer.trimToNull(request.getSlug());
        if (pageRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A page with slug '" + slug + "' already exists");
        }

        Page page = new Page();
        page.setTitle(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getTitle()));
        page.setSlug(slug);
        page.setContent(InputSanitizer.sanitizePageHtml(request.getContent()));

        String status = InputSanitizer.trimToNull(request.getStatus());
        page.setStatus(status != null ? status : "draft");

        page.setMetaTitle(InputSanitizer.trimToNull(request.getMetaTitle()));
        page.setMetaDescription(InputSanitizer.trimToNull(request.getMetaDescription()));
        page.setOgImageUrl(InputSanitizer.trimToNull(request.getOgImageUrl()));

        return toResponse(pageRepository.save(page));
    }

    @Transactional
    public PageResponse updatePage(Long id, PageRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Page not found with id: " + id));

        String slug = InputSanitizer.trimToNull(request.getSlug());
        if (pageRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A page with slug '" + slug + "' already exists");
        }

        page.setTitle(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getTitle()));
        page.setSlug(slug);
        page.setContent(InputSanitizer.sanitizePageHtml(request.getContent()));
        if (request.getStatus() != null) {
            page.setStatus(InputSanitizer.trimToNull(request.getStatus()));
        }

        page.setMetaTitle(InputSanitizer.trimToNull(request.getMetaTitle()));
        page.setMetaDescription(InputSanitizer.trimToNull(request.getMetaDescription()));
        page.setOgImageUrl(InputSanitizer.trimToNull(request.getOgImageUrl()));

        return toResponse(pageRepository.save(page));
    }

    @Transactional
    public void deletePage(Long id) {
        if (!pageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found with id: " + id);
        }
        pageRepository.deleteById(id);
    }

    private PageResponse toResponse(Page page) {
        return new PageResponse(
            page.getId(),
            page.getTitle(),
            page.getSlug(),
            page.getContent(),
            page.getStatus(),
            page.getMetaTitle(),
            page.getMetaDescription(),
            page.getOgImageUrl(),
            page.getCreatedAt(),
            page.getUpdatedAt()
        );
    }
}
