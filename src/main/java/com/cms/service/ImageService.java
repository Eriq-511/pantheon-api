package com.cms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cms.dto.ImageResponse;
import com.cms.dto.ImageUpdateRequest;
import com.cms.model.Image;
import com.cms.repository.ImageRepository;
import com.cms.util.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private static final List<String> ALLOWED_CONTENT_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB

    private final ImageRepository imageRepository;
    private final Cloudinary cloudinary;

    public ImageService(ImageRepository imageRepository, Cloudinary cloudinary) {
        this.imageRepository = imageRepository;
        this.cloudinary = cloudinary;
    }

    public List<ImageResponse> getAllImages() {
        return imageRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public ImageResponse uploadImage(MultipartFile file, String altText) {
        validateFile(file);

        String sanitizedAltText = InputSanitizer.normalizeWhitespaceToSingleSpaces(altText);

        try {
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "pantheon-cms",
                    "resource_type", "image"
            );

            Map<String, Object> result = cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);

                Image image = new Image();
                image.setFilename(file.getOriginalFilename());
                image.setCloudinaryUrl((String) result.get("secure_url"));
                image.setCloudinaryPublicId((String) result.get("public_id"));
                image.setAltText(sanitizedAltText);

            return toResponse(imageRepository.save(image));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload image: " + e.getMessage());
        }
    }

    @Transactional
    public ImageResponse updateImage(Long id, ImageUpdateRequest request) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Image not found with id: " + id));

        image.setAltText(InputSanitizer.normalizeWhitespaceToSingleSpaces(request.getAltText()));
        return toResponse(imageRepository.save(image));
    }

    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Image not found with id: " + id));

        try {
            cloudinary.uploader().destroy(image.getCloudinaryPublicId(),
                    ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException | RuntimeException e) {
            // Log but continue – the DB record must still be removed.
            log.warn("Could not delete from Cloudinary (public_id={}): {}",
                    image.getCloudinaryPublicId(), e.getMessage());
        }

        imageRepository.deleteById(id);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File type not allowed. Accepted: jpg, png, webp");
        }
    }

    private ImageResponse toResponse(Image image) {
        return new ImageResponse(
            image.getId(),
            image.getFilename(),
            image.getCloudinaryUrl(),
            image.getCloudinaryPublicId(),
            image.getAltText(),
            image.getUploadedAt()
        );
    }
}
