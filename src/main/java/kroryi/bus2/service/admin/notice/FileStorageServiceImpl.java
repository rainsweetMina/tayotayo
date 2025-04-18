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

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public List<NoticeFile> storeFiles(List<MultipartFile> files, Notice notice) {
        List<NoticeFile> storedFiles = new ArrayList<>();

        String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 존재하지 않으면 디렉토리 생성
        }


        for (MultipartFile file : files) {
            try {
                String originalName = file.getOriginalFilename();
                String extension = originalName.substring(originalName.lastIndexOf('.') + 1);
                String storedName = UUID.randomUUID() + "." + extension;

                File targetFile = new File(uploadDirPath + File.separator + storedName);

                if (file.getSize() > 10 * 1024 * 1024 && file.getContentType().startsWith("image/")) {
                    BufferedImage originalImage = ImageIO.read(file.getInputStream());
                    BufferedImage resized = resizeImage(originalImage, 1280, 720);
                    ImageIO.write(resized, extension, targetFile);
                } else {
                    file.transferTo(targetFile); // ⬅️ 여기만 1번
                }

                storedFiles.add(NoticeFile.builder()
                        .originalName(originalName)
                        .storedName(storedName)
                        .fileSize(file.getSize())
                        .fileType(file.getContentType())
                        .notice(notice)
                        .build());

                log.info("📂 저장 위치: {}", targetFile.getAbsolutePath());

            } catch (IOException e) {
                log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
            }
        }

        return storedFiles;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(scaledImage, 0, 0, null);
        return resized;
    }
}
