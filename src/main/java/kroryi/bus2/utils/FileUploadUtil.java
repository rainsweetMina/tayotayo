package kroryi.bus2.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // ✅ application.properties에서 경로 주입
    @Value("${file.upload.ad-location}")
    private String basePath;

    /**
     * 광고 이미지를 ad/ 하위에 저장
     * @param file MultipartFile
     * @return 저장된 상대 경로 (예: ad/uuid.png)
     */
    public String saveAdImage(MultipartFile file) {
        // ✅ 디렉토리 생성
        String uploadDir = basePath; // ex: C:/project/uploads/ad
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // ✅ ad 디렉토리 자동 생성
        }

        // ✅ 확장자 추출 + UUID 파일명
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID() + extension;

        // ✅ 실제 저장
        File dest = new File(dir, savedFilename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

        // ✅ 상대 경로 반환 (DB에 저장할 값)
        return "ad/" + savedFilename;
    }
}