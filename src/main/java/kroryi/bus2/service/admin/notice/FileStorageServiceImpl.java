package kroryi.bus2.service.admin.notice;

import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.awt.Image.SCALE_SMOOTH;
import java.awt.Image;
import java.awt.image.BufferedImage;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/notices/";

    @Override
    public List<NoticeFile> storeFiles(List<MultipartFile> files, Notice notice) {
        List<NoticeFile> storedFiles = new ArrayList<>();
        
        log.info("📂 파일 저장 시작 - 공지사항 ID: {}, 파일 개수: {}", notice.getId(), files.size());

        // 업로드 디렉토리 생성
        String rootPath = System.getProperty("user.dir");
        String noticePath = "uploads" + File.separator + "notices";
        String uploadDirPath = rootPath + File.separator + noticePath;
        
        // 디렉토리 생성 (notices 디렉토리까지)
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs(); // 존재하지 않으면 디렉토리 생성
            log.info("📂 업로드 디렉토리 생성: {} (성공: {})", uploadDirPath, created);
            
            if (!created) {
                log.error("❌ 업로드 디렉토리 생성 실패: {}", uploadDirPath);
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.");
            }
        }

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    log.warn("⚠️ 빈 파일 건너뛰기: {}", file.getOriginalFilename());
                    continue;
                }
                
                String originalName = file.getOriginalFilename();
                if (originalName == null || originalName.isEmpty()) {
                    log.warn("⚠️ 파일명이 없는 파일 건너뛰기");
                    continue;
                }
                
                int dotIndex = originalName.lastIndexOf('.');
                String extension = (dotIndex > 0) ? originalName.substring(dotIndex + 1) : "";
                String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

                File targetFile = new File(uploadDirPath + File.separator + storedName);
                log.info("📂 파일 저장 시도 - 원본명: {}, 저장명: {}, 저장경로: {}", originalName, storedName, targetFile.getAbsolutePath());

                try {
                    if (file.getSize() > 10 * 1024 * 1024 && file.getContentType() != null && file.getContentType().startsWith("image/")) {
                        BufferedImage originalImage = ImageIO.read(file.getInputStream());
                        if (originalImage != null) {
                            BufferedImage resized = resizeImage(originalImage, 1280, 720);
                            ImageIO.write(resized, extension, targetFile);
                            log.info("📂 이미지 리사이징 후 저장 완료");
                        } else {
                            file.transferTo(targetFile);
                            log.info("📂 이미지 리사이징 실패, 원본 파일 저장 완료");
                        }
                    } else {
                        file.transferTo(targetFile);
                        log.info("📂 파일 저장 완료");
                    }
                } catch (IOException e) {
                    log.error("❌ 파일 저장 중 IO 오류: {}", e.getMessage(), e);
                    throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
                }

                NoticeFile noticeFile = NoticeFile.builder()
                        .originalName(originalName)
                        .storedName(storedName)
                        .fileSize(file.getSize())
                        .fileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .notice(notice)
                        .build();
                
                storedFiles.add(noticeFile);
                log.info("📂 파일 정보 생성 완료: {}", noticeFile);

            } catch (Exception e) {
                log.error("❌ 파일 처리 중 예외 발생: {}", file.getOriginalFilename(), e);
                // 예외를 던지지 않고 계속 진행 - 일부 파일만 실패할 경우 다른 파일은 처리
            }
        }

        log.info("📂 파일 저장 완료 - 저장된 파일 수: {}", storedFiles.size());
        return storedFiles;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(scaledImage, 0, 0, null);
        return resized;
    }
}
