package kroryi.bus2.controller;

import kroryi.bus2.entity.FoundItem;
import kroryi.bus2.entity.Photo;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/found")
public class FoundItemImageController {

    private final FoundItemRepository foundItemRepository;
    private final PhotoRepository photoRepository;

    private final Path rootLocation = Paths.get("uploads/found");

    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
                                         @RequestParam("image") MultipartFile image) {
        try {
            // 🔹 용량 제한 (500KB)
            if (image.getSize() > 500 * 1024) {
                return ResponseEntity.badRequest().body("이미지 용량은 500KB 이하만 허용됩니다.");
            }

            // 🔹 파일 타입 제한
            String contentType = image.getContentType();
            if (!("image/jpeg".equals(contentType) || "image/png".equals(contentType))) {
                return ResponseEntity.badRequest().body("jpg 또는 png만 허용됩니다.");
            }

            // 🔹 습득물 존재 확인
            FoundItem foundItem = foundItemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 습득물이 없습니다."));

            // 🔹 파일 저장
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Files.createDirectories(rootLocation);
            Path destination = rootLocation.resolve(filename);
            Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // 🔹 기존 사진 있으면 제거
            if (foundItem.getPhoto() != null) {
                photoRepository.delete(foundItem.getPhoto());
            }

            // 🔹 DB 저장
            Photo photo = Photo.builder()
                    .url("/uploads/found/" + filename)
                    .foundItem(foundItem)
                    .build();

            photoRepository.save(photo);

            // 🔹 엔티티 연결 (양방향)
            foundItem.setPhoto(photo);

            return ResponseEntity.ok("사진 업로드 완료");

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 저장 실패: " + e.getMessage());
        }
    }
}


