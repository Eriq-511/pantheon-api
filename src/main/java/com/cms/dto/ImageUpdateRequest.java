package com.cms.dto;

import jakarta.validation.constraints.Size;

public class ImageUpdateRequest {
    @Size(max = 255, message = "Alt text must be at most 255 characters")
    private String altText;

    public ImageUpdateRequest() {
    }

    public ImageUpdateRequest(String altText) {
        this.altText = altText;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }
}
