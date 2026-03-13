package com.cms.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cms.dto.ProductImageUpdateRequest;
import com.cms.dto.ProductRequest;
import com.cms.dto.ProductResponse;
import com.cms.model.Product;
import com.cms.repository.ProductRepository;
import com.cms.util.InputSanitizer;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAll(String category) {
        List<Product> products;
        String normalizedCategory = InputSanitizer.trimToNull(category);
        if (normalizedCategory == null) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByCategoryIgnoreCase(normalizedCategory);
        }
        return products.stream().map(this::toResponse).toList();
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        product.setRatingRate(BigDecimal.ZERO);
        product.setRatingCount(0);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id));
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateImage(Long id, ProductImageUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id));
        product.setImage(InputSanitizer.trimToNull(request.getImage()));
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setTitle(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getTitle()));
        product.setDescription(InputSanitizer.trimToNull(request.getDescription()));
        product.setCategory(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getCategory()));

        Double requestPrice = request.getPrice();
        if (requestPrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required");
        }
        product.setPrice(BigDecimal.valueOf(requestPrice));

        String normalizedImage = InputSanitizer.trimToNull(request.getImage());
        if (normalizedImage == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image URL is required");
        }
        product.setImage(normalizedImage);
    }

    private ProductResponse toResponse(Product product) {
        Integer ratingCount = product.getRatingCount();
        BigDecimal productPrice = product.getPrice();
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                productPrice != null ? productPrice.doubleValue() : 0.0,
                product.getDescription(),
                product.getCategory(),
                product.getImage(),
                new ProductResponse.ProductRatingResponse(
                        product.getRatingRate() != null ? product.getRatingRate().doubleValue() : 0.0,
                        ratingCount == null ? 0 : ratingCount),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
