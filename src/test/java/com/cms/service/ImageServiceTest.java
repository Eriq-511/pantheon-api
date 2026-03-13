package com.cms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cms.AbstractIntegrationTest;
import com.cms.dto.ImageResponse;
import com.cms.dto.ImageUpdateRequest;
import com.cms.model.Image;
import com.cms.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("removal")
class ImageServiceTest extends AbstractIntegrationTest {

    // Replace the Cloudinary bean in the context with a mock so no real
    // HTTP calls are made to Cloudinary during integration tests.
    @MockBean Cloudinary cloudinary;

    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;

    private Uploader uploader;

    //  getAll 

    @Test
        public void getAllImages_shouldReturnPersistedImages() {
        imageRepository.save(image("photo.jpg",
                "https://res.cloudinary.com/test/photo.jpg",
                "pantheon-cms/photo",
                "A photo"));

        List<ImageResponse> result = imageService.getAllImages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFilename()).isEqualTo("photo.jpg");
    }

    @Test
        public void getAllImages_shouldReturnEmpty_whenNoImages() {
        assertThat(imageService.getAllImages()).isEmpty();
    }

    //  upload 

    @Test
        public void uploadImage_shouldCallCloudinaryAndPersistToDb() throws IOException {
                initCloudinaryMock();
        MockMultipartFile file = new MockMultipartFile(
                "file", "upload.jpg", "image/jpeg", "image-bytes".getBytes());

        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/test/upload.jpg",
                        "public_id", "pantheon-cms/upload"));

        ImageResponse result = imageService.uploadImage(file, "Upload test");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getFilename()).isEqualTo("upload.jpg");
        assertThat(result.getCloudinaryUrl()).contains("upload.jpg");
        assertThat(result.getAltText()).isEqualTo("Upload test");
        assertThat(imageRepository.existsById(result.getId())).isTrue();
        verify(uploader).upload(any(byte[].class), any(Map.class));
    }

    @Test
        public void uploadImage_shouldThrow400_whenFileIsEmpty() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> imageService.uploadImage(empty, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("empty");
    }

    @Test
        public void uploadImage_shouldThrow400_whenFileTooLarge() {
        byte[] big = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", big);

        assertThatThrownBy(() -> imageService.uploadImage(file, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("5MB");
    }

    @Test
        public void uploadImage_shouldThrow400_whenContentTypeNotAllowed() {
        MockMultipartFile gif = new MockMultipartFile(
                "file", "anim.gif", "image/gif", "gif-content".getBytes());

        assertThatThrownBy(() -> imageService.uploadImage(gif, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
        public void uploadImage_shouldThrow400_whenContentTypeIsNull() {
        MockMultipartFile noType = new MockMultipartFile(
                "file", "file.bin", null, "data".getBytes());

        assertThatThrownBy(() -> imageService.uploadImage(noType, null))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  update 

    @Test
        public void updateImage_shouldUpdateAltTextInDb() {
        Image saved = imageRepository.save(image("photo.jpg",
                "https://res.cloudinary.com/test/photo.jpg",
                "pantheon-cms/photo",
                "Original alt"));

        ImageUpdateRequest req = new ImageUpdateRequest();
        req.setAltText("New alt text");

        ImageResponse result = imageService.updateImage(saved.getId(), req);

        assertThat(result.getAltText()).isEqualTo("New alt text");
        assertThat(imageRepository.findById(saved.getId()).get().getAltText())
                .isEqualTo("New alt text");
    }

    @Test
        public void updateImage_shouldThrow404_whenImageNotFound() {
        ImageUpdateRequest req = new ImageUpdateRequest();
        req.setAltText("anything");

        assertThatThrownBy(() -> imageService.updateImage(99999L, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    //  delete 

    @Test
        public void deleteImage_shouldDestroyFromCloudinaryAndRemoveFromDb() throws IOException {
                initCloudinaryMock();
        Image saved = imageRepository.save(image("photo.jpg",
                "https://res.cloudinary.com/test/photo.jpg",
                "pantheon-cms/photo",
                "A photo"));

        when(uploader.destroy(eq("pantheon-cms/photo"), any(Map.class)))
                .thenReturn(Map.of("result", "ok"));

        imageService.deleteImage(saved.getId());

        verify(uploader).destroy(eq("pantheon-cms/photo"), any(Map.class));
        assertThat(imageRepository.existsById(saved.getId())).isFalse();
    }

    @Test
        public void deleteImage_shouldStillDeleteFromDb_whenCloudinaryFails() throws IOException {
                initCloudinaryMock();
        Image saved = imageRepository.save(image("photo.jpg",
                "https://res.cloudinary.com/test/photo.jpg",
                "pantheon-cms/photo",
                "A photo"));

        when(uploader.destroy(eq("pantheon-cms/photo"), any(Map.class)))
                .thenThrow(new IOException("Cloudinary unavailable"));

        // Should NOT throw -- warning is logged, DB delete still happens.
        imageService.deleteImage(saved.getId());

        assertThat(imageRepository.existsById(saved.getId())).isFalse();
    }

    @Test
        public void deleteImage_shouldThrow404_whenImageNotFound() {
        assertThatThrownBy(() -> imageService.deleteImage(99999L))
                .isInstanceOf(ResponseStatusException.class);
    }

        private static Image image(String filename, String cloudinaryUrl, String cloudinaryPublicId, String altText) {
                Image image = new Image();
                image.setFilename(filename);
                image.setCloudinaryUrl(cloudinaryUrl);
                image.setCloudinaryPublicId(cloudinaryPublicId);
                image.setAltText(altText);
                return image;
        }

        private void initCloudinaryMock() {
                uploader = Mockito.mock(Uploader.class);
                when(cloudinary.uploader()).thenReturn(uploader);
        }
}
