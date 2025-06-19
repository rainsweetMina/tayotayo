package kroryi.bus2.controller.admin.lowfloorbus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.lowfloorbus.LowFloorBusResponseDTO;
import kroryi.bus2.service.admin.lowfloorbus.LowFloorBusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Tag(name = "저상버스 대체 안내", description = "저상버스 대체 안내 조회 API")
@RestController
@RequestMapping("/api/public/lowfloorbuses")
@RequiredArgsConstructor
@Log4j2
public class PublicLowFloorBusApiController {

    private final LowFloorBusService lowFloorBusService;

    @Operation(summary = "저상버스 대체 안내 전체 목록 조회")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LowFloorBusResponseDTO>> getAllLowFloorBuses() {
        log.info("🟢 PUBLIC API 호출됨 - /api/public/lowfloorbuses");
        return ResponseEntity.ok(lowFloorBusService.getAllLowFloorBuses());
    }

    @Operation(summary = "저상버스 대체 안내 상세 조회")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LowFloorBusResponseDTO> getLowFloorBus(@PathVariable Long id) {
        log.info("🟢 PUBLIC API 호출됨 - /api/public/lowfloorbuses/{}", id);
        LowFloorBusResponseDTO lowFloorBus = lowFloorBusService.getLowFloorBusById(id);
        log.info("🟢 저상버스 대체 안내 조회 완료 - ID: {}, 제목: {}, 조회수: {}", id, lowFloorBus.getTitle(), lowFloorBus.getViewCount());
        return ResponseEntity.ok(lowFloorBus);
    }

    @Operation(summary = "저상버스 대체 안내 첨부파일 다운로드")
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            // 파일 ID로 파일 정보 조회 로직 필요
            // 여기서는 임시로 경로를 하드코딩
            Path filePath = Paths.get("uploads/lowfloorbuses/1/sample.pdf");
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("📛 파일 경로 오류", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("📛 파일 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "저상버스 대체 안내 첨부파일 다운로드")
    @GetMapping("/files/{lowFloorBusId}/{fileName}")
    public ResponseEntity<Resource> downloadLowFloorBusFile(
            @PathVariable Long lowFloorBusId,
            @PathVariable String fileName
    ) {
        try {
            Path filePath = Paths.get("uploads/lowfloorbuses/" + lowFloorBusId).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("📛 파일 경로 오류", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("📛 파일 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "저상버스 대체 안내 첨부파일 다운로드")
    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadFileWithId(@PathVariable Long id) {
        try {
            // 파일 ID로 파일 정보 조회 로직 필요
            // 여기서는 임시로 경로를 하드코딩
            Path filePath = Paths.get("uploads/lowfloorbuses/1/sample.pdf");
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("📛 파일 경로 오류", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("📛 파일 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 