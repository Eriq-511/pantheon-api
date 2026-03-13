package com.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductImageUpdateRequest {

    @NotBlank(message = "Image URL is required")
    @Size(max = 1000, message = "Image URL is too long")
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
