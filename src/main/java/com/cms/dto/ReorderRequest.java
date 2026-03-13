package com.cms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ReorderRequest {
    @NotNull(message = "Items list is required")
    @Size(min = 1, message = "Items list is required")
    @Valid
    private List<ReorderItem> items;

    public ReorderRequest() {
    }

    public ReorderRequest(List<ReorderItem> items) {
        this.items = items;
    }

    public List<ReorderItem> getItems() {
        return items;
    }

    public void setItems(List<ReorderItem> items) {
        this.items = items;
    }

    public static class ReorderItem {
        @NotNull(message = "Menu item id is required")
        @Positive(message = "Menu item id must be a positive number")
        private Long id;

        @NotNull(message = "Order index is required")
        @Min(value = 0, message = "Order index must be 0 or greater")
        private Integer orderIndex;

        public ReorderItem() {
        }

        public ReorderItem(Long id, Integer orderIndex) {
            this.id = id;
            this.orderIndex = orderIndex;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
        }
    }
}
