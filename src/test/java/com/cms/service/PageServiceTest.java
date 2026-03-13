package com.cms.service;

import com.cms.AbstractIntegrationTest;
import com.cms.dto.PageRequest;
import com.cms.dto.PageResponse;
import com.cms.repository.PageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageServiceTest extends AbstractIntegrationTest {

    @Autowired PageService pageService;
    @Autowired PageRepository pageRepository;

    //  getAll 

    @Test
    public void getAllPages_shouldReturnMappedResponseList() {
        pageService.createPage(req("Home", "home", "<p>Welcome</p>", "published"));

        List<PageResponse> result = pageService.getAllPages(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("home");
        assertThat(result.get(0).getTitle()).isEqualTo("Home");
    }

    @Test
    public void getAllPages_shouldReturnEmptyList_whenNoPages() {
        assertThat(pageService.getAllPages(true)).isEmpty();
    }

    //  getBySlug 

    @Test
    public void getPageBySlug_shouldReturnPage_whenFound() {
        pageService.createPage(req("About", "about", "Content", "published"));

        PageResponse result = pageService.getPageBySlug("about");

        assertThat(result.getSlug()).isEqualTo("about");
        assertThat(result.getStatus()).isEqualTo("published");
    }

    @Test
    public void getPageBySlug_shouldThrow404_whenNotFound() {
        assertThatThrownBy(() -> pageService.getPageBySlug("missing"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Page not found");
    }

    //  getById 

    @Test
    public void getPageById_shouldReturnPage_whenFound() {
        PageResponse created = pageService.createPage(req("Contact", "contact", "", "draft"));

        PageResponse result = pageService.getPageById(created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getSlug()).isEqualTo("contact");
    }

    @Test
    public void getPageById_shouldThrow404_whenNotFound() {
        assertThatThrownBy(() -> pageService.getPageById(99999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  create 

    @Test
    public void createPage_shouldPersistAndReturnResponse() {
        PageResponse result = pageService.createPage(req("Blog", "blog", "<p>Posts</p>", "published"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getSlug()).isEqualTo("blog");
        assertThat(result.getTitle()).isEqualTo("Blog");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(pageRepository.existsBySlug("blog")).isTrue();
    }

    @Test
    public void createPage_shouldDefaultStatusToDraft_whenStatusNull() {
        PageResponse result = pageService.createPage(req("No Status", "no-status", "", null));

        assertThat(result.getStatus()).isEqualTo("draft");
    }

    @Test
    public void createPage_shouldThrow409_whenSlugAlreadyExists() {
        pageService.createPage(req("Home", "home", "", "draft"));

        assertThatThrownBy(() -> pageService.createPage(req("Home 2", "home", "", "draft")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    //  update 

    @Test
    public void updatePage_shouldUpdateFieldsInDb() {
        PageResponse created = pageService.createPage(req("Old Title", "old-slug", "Old", "draft"));

        PageResponse updated = pageService.updatePage(created.getId(),
                req("New Title", "new-slug", "New", "published"));

        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getSlug()).isEqualTo("new-slug");
        assertThat(updated.getStatus()).isEqualTo("published");
        assertThat(pageRepository.existsBySlug("old-slug")).isFalse();
        assertThat(pageRepository.existsBySlug("new-slug")).isTrue();
    }

    @Test
    public void updatePage_shouldThrow404_whenPageNotFound() {
        assertThatThrownBy(() -> pageService.updatePage(99999L, req("X", "x", "", "draft")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void updatePage_shouldThrow409_whenNewSlugTakenByAnotherPage() {
        pageService.createPage(req("About", "about", "", "draft"));
        PageResponse home = pageService.createPage(req("Home", "home", "", "draft"));

        assertThatThrownBy(() -> pageService.updatePage(home.getId(),
                req("Home", "about", "", "draft")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    //  delete 

    @Test
    public void deletePage_shouldRemoveFromDb() {
        PageResponse created = pageService.createPage(req("To Delete", "to-delete", "", "draft"));

        pageService.deletePage(created.getId());

        assertThat(pageRepository.existsById(created.getId())).isFalse();
    }

    @Test
    public void deletePage_shouldThrow404_whenPageNotFound() {
        assertThatThrownBy(() -> pageService.deletePage(99999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  helper 

    private PageRequest req(String title, String slug, String content, String status) {
        PageRequest r = new PageRequest();
        r.setTitle(title);
        r.setSlug(slug);
        r.setContent(content);
        r.setStatus(status);
        return r;
    }
}
