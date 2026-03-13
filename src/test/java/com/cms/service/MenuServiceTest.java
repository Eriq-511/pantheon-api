package com.cms.service;

import com.cms.AbstractIntegrationTest;
import com.cms.dto.MenuItemRequest;
import com.cms.dto.MenuItemResponse;
import com.cms.dto.ReorderRequest;
import com.cms.model.Page;
import com.cms.repository.MenuItemRepository;
import com.cms.repository.PageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuServiceTest extends AbstractIntegrationTest {

    @Autowired MenuService menuService;
    @Autowired MenuItemRepository menuItemRepository;
    @Autowired PageRepository pageRepository;

    //  getAll 

    @Test
    public void getAllMenuItems_shouldReturnOrderedList() {
        menuService.createMenuItem(req("Home", "/", null, 0, null));
        menuService.createMenuItem(req("Blog", "/blog", null, 1, null));

        List<MenuItemResponse> result = menuService.getAllMenuItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderIndex())
                .isLessThanOrEqualTo(result.get(1).getOrderIndex());
    }

    @Test
    public void getAllMenuItems_shouldReturnEmpty_whenNoItems() {
        assertThat(menuService.getAllMenuItems()).isEmpty();
    }

    //  create 

    @Test
    public void createMenuItem_withoutPage_shouldPersistAndReturn() {
        MenuItemResponse result = menuService.createMenuItem(req("Home", "/", null, 0, null));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getLabel()).isEqualTo("Home");
        assertThat(result.getPageId()).isNull();
        assertThat(menuItemRepository.existsById(result.getId())).isTrue();
    }

    @Test
    public void createMenuItem_withPageId_shouldLinkPage() {
        Page linkedPage = createPublishedPage("about");
        MenuItemResponse result = menuService.createMenuItem(
                req("About", null, null, 1, linkedPage.getId()));

        assertThat(result.getPageId()).isEqualTo(linkedPage.getId());
        assertThat(result.getPageSlug()).isEqualTo("about");
    }

    @Test
    public void createMenuItem_shouldThrow404_whenPageIdNotFound() {
        assertThatThrownBy(() -> menuService.createMenuItem(req("X", null, null, 0, 99999L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Page not found");
    }

    @Test
    public void createMenuItem_shouldDefaultOrderIndex_whenNotProvided() {
        MenuItemRequest r = new MenuItemRequest();
        r.setLabel("Contact");

        MenuItemResponse result = menuService.createMenuItem(r);

        assertThat(result.getOrderIndex()).isEqualTo(0);
    }

    //  update 

    @Test
    public void updateMenuItem_shouldUpdateAllFields() {
        MenuItemResponse created = menuService.createMenuItem(req("Home", "/", null, 0, null));

        MenuItemResponse updated = menuService.updateMenuItem(created.getId(),
                req("Home v2", "/home", "home-icon", 5, null));

        assertThat(updated.getLabel()).isEqualTo("Home v2");
        assertThat(updated.getUrl()).isEqualTo("/home");
        assertThat(updated.getIcon()).isEqualTo("home-icon");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }

    @Test
    public void updateMenuItem_shouldLinkPage_whenPageIdProvided() {
        Page linkedPage = createPublishedPage("about");
        MenuItemResponse created = menuService.createMenuItem(req("Link", "/", null, 0, null));

        MenuItemResponse updated = menuService.updateMenuItem(created.getId(),
                req("About Link", null, null, 0, linkedPage.getId()));

        assertThat(updated.getPageId()).isEqualTo(linkedPage.getId());
    }

    @Test
    public void updateMenuItem_shouldThrow404_whenNotFound() {
        assertThatThrownBy(() -> menuService.updateMenuItem(99999L,
                req("X", null, null, 0, null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  reorder 

    @Test
    public void reorderMenuItems_shouldUpdateOrderIndexInDb() {
        MenuItemResponse a = menuService.createMenuItem(req("A", "/a", null, 0, null));
        MenuItemResponse b = menuService.createMenuItem(req("B", "/b", null, 1, null));

        ReorderRequest.ReorderItem ri1 = new ReorderRequest.ReorderItem();
        ri1.setId(a.getId());
        ri1.setOrderIndex(1);
        ReorderRequest.ReorderItem ri2 = new ReorderRequest.ReorderItem();
        ri2.setId(b.getId());
        ri2.setOrderIndex(0);

        ReorderRequest reorderReq = new ReorderRequest();
        reorderReq.setItems(List.of(ri1, ri2));

        menuService.reorderMenuItems(reorderReq);

        assertThat(menuItemRepository.findById(a.getId()).get().getOrderIndex()).isEqualTo(1);
        assertThat(menuItemRepository.findById(b.getId()).get().getOrderIndex()).isEqualTo(0);
    }

    @Test
    public void reorderMenuItems_shouldThrow400_whenItemsListNull() {
        ReorderRequest rr = new ReorderRequest();
        rr.setItems(null);

        assertThatThrownBy(() -> menuService.reorderMenuItems(rr))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Items list is required");
    }

    @Test
    public void reorderMenuItems_shouldThrow400_whenItemsListEmpty() {
        ReorderRequest rr = new ReorderRequest();
        rr.setItems(List.of());

        assertThatThrownBy(() -> menuService.reorderMenuItems(rr))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  delete 

    @Test
    public void deleteMenuItem_shouldRemoveFromDb() {
        MenuItemResponse created = menuService.createMenuItem(req("Delete Me", "/del", null, 0, null));
        Long id = created.getId();

        menuService.deleteMenuItem(id);

        assertThat(menuItemRepository.existsById(id)).isFalse();
    }

    @Test
    public void deleteMenuItem_shouldThrow404_whenNotFound() {
        assertThatThrownBy(() -> menuService.deleteMenuItem(99999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  helper 

    private MenuItemRequest req(String label, String url, String icon,
                                int orderIndex, Long pageId) {
        MenuItemRequest r = new MenuItemRequest();
        r.setLabel(label);
        r.setUrl(url);
        r.setIcon(icon);
        r.setOrderIndex(orderIndex);
        r.setPageId(pageId);
        return r;
    }

    private Page createPublishedPage(String slug) {
        Page page = new Page();
        page.setTitle("About");
        page.setSlug(slug);
        page.setContent("");
        page.setStatus("published");
        return pageRepository.save(page);
    }
}
