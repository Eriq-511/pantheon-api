package com.cms.service;

import com.cms.dto.MenuItemRequest;
import com.cms.dto.MenuItemResponse;
import com.cms.dto.ReorderRequest;
import com.cms.model.MenuItem;
import com.cms.model.Page;
import com.cms.repository.MenuItemRepository;
import com.cms.repository.PageRepository;
import com.cms.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final PageRepository pageRepository;

    public MenuService(MenuItemRepository menuItemRepository, PageRepository pageRepository) {
        this.menuItemRepository = menuItemRepository;
        this.pageRepository = pageRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        MenuItem item = buildMenuItem(new MenuItem(), request);
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Menu item not found with id: " + id));
        buildMenuItem(item, request);
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void reorderMenuItems(ReorderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Items list is required");
        }

        for (ReorderRequest.ReorderItem reorderItem : request.getItems()) {
            MenuItem item = menuItemRepository.findById(reorderItem.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Menu item not found with id: " + reorderItem.getId()));
            Integer orderIndex = reorderItem.getOrderIndex();
            if (orderIndex == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order index is required for menu item id: " + reorderItem.getId());
            }
            item.setOrderIndex(orderIndex);
            menuItemRepository.save(item);
        }
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Menu item not found with id: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItem buildMenuItem(MenuItem item, MenuItemRequest request) {
        item.setLabel(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getLabel()));
        item.setUrl(InputSanitizer.trimToNull(request.getUrl()));
        item.setIcon(InputSanitizer.trimToNull(request.getIcon()));
        if (request.getOrderIndex() != null) {
            item.setOrderIndex(request.getOrderIndex());
        }

        if (request.getPageId() != null) {
            Page page = pageRepository.findById(request.getPageId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Page not found with id: " + request.getPageId()));
            item.setPage(page);
        } else {
            item.setPage(null);
        }

        return item;
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
            item.getId(),
            item.getLabel(),
            item.getUrl(),
            item.getIcon(),
            item.getOrderIndex(),
            item.getPage() != null ? item.getPage().getId() : null,
            item.getPage() != null ? item.getPage().getSlug() : null
        );
    }
}
