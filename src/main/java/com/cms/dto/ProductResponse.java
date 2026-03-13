package com.cms.dto;

import java.time.LocalDateTime;

public class ProductResponse {

    private final Long id;
    private final String title;
    private final Double price;
    private final String description;
    private final String category;
    private final String image;
    private final ProductRatingResponse rating;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProductResponse(Long id, String title, Double price, String description, String category, String image,
            ProductRatingResponse rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.category = category;
        this.image = image;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public ProductRatingResponse getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class ProductRatingResponse {
        private final double rate;
        private final int count;

        public ProductRatingResponse(double rate, int count) {
            this.rate = rate;
            this.count = count;
        }

        public double getRate() {
            return rate;
        }

        public int getCount() {
            return count;
        }
    }
}
