package com.cms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
public class MenuItemRequest {

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must be at most 100 characters")
    private String label;

    @Size(max = 255, message = "URL must be at most 255 characters")
    private String url;

    @Size(max = 100, message = "Icon must be at most 100 characters")
    private String icon;

    @Min(value = 0, message = "Order index must be 0 or greater")
    private Integer orderIndex;

    @Positive(message = "Page id must be a positive number")
    private Long pageId;

    public MenuItemRequest() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }
}
