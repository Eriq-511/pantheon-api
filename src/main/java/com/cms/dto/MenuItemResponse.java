package com.cms.dto;

public class MenuItemResponse {
    private Long id;
    private String label;
    private String url;
    private String icon;
    private Integer orderIndex;
    private Long pageId;
    private String pageSlug;

    public MenuItemResponse() {
    }

    public MenuItemResponse(Long id, String label, String url, String icon, Integer orderIndex, Long pageId,
                            String pageSlug) {
        this.id = id;
        this.label = label;
        this.url = url;
        this.icon = icon;
        this.orderIndex = orderIndex;
        this.pageId = pageId;
        this.pageSlug = pageSlug;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPageSlug() {
        return pageSlug;
    }

    public void setPageSlug(String pageSlug) {
        this.pageSlug = pageSlug;
    }
}
