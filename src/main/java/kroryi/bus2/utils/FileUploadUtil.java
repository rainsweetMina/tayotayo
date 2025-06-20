package kroryi.bus2.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class FileUploadUtil {

    // ✅ application.properties에서 경로 주입
    @Value("${file.upload.ad-location}")
    private String basePath;

    // 허용된 이미지 타입
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "application/octet-stream"
    );

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    // 최대 파일 크기 (20MB)
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    /**
     * 광고 이미지를 ad/ 하위에 저장
     * @param file MultipartFile
     * @return 저장된 상대 경로 (예: ad/uuid.png)
     */
    public String saveAdImage(MultipartFile file) {
        // 파일 유효성 검사
        validateImageFile(file);
        String baseDir = System.getProperty("user.dir"); // 현재 프로젝트 디렉터리

        // ✅ 디렉토리 생성
        String uploadDir = baseDir+"/"+ basePath;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다: " + uploadDir);
            }
            log.info("업로드 디렉토리 생성: {}", uploadDir);
        }

        // ✅ 확장자 추출 + UUID 파일명
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename);
        String savedFilename = UUID.randomUUID().toString() + extension;

        // ✅ 실제 저장
        File dest = new File(dir, savedFilename);
        try {
            file.transferTo(dest);
            log.info("이미지 파일 저장 성공: {} -> {}", originalFilename, dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("이미지 파일 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 저장 실패: " + e.getMessage(), e);
        }

        // ✅ 상대 경로 반환 (DB에 저장할 값)
        return "ad/" + savedFilename;
    }

    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (MAX_FILE_SIZE / 1024 / 1024) + "MB까지 허용됩니다.");
        }

        // 파일 타입 검사 (더 유연하게)
        String contentType = file.getContentType();
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            if (!ALLOWED_IMAGE_TYPES.contains(lowerContentType) && !lowerContentType.startsWith("image/")) {
                throw new IllegalArgumentException("지원하지 않는 파일 타입입니다. 허용된 타입: " + ALLOWED_IMAGE_TYPES);
            }
        }

        // 파일 확장자 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다. 허용된 확장자: " + ALLOWED_EXTENSIONS);
            }
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("파일 확장자가 없습니다: " + filename);
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path fullPath = Paths.get(basePath, relativePath.replace("ad/", ""));
            boolean deleted = Files.deleteIfExists(fullPath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", fullPath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", fullPath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String relativePath) {
        Path fullPath = Paths.get(basePath, relativePath.replace("ad/", ""));
        return Files.exists(fullPath);
    }

    /**
     * 파일 크기 확인
     */
    public long getFileSize(String relativePath) {
        try {
            Path fullPath = Paths.get(basePath, relativePath.replace("ad/", ""));
            return Files.size(fullPath);
        } catch (IOException e) {
            log.error("파일 크기 확인 실패: {}", e.getMessage(), e);
            return -1;
        }
    }
}