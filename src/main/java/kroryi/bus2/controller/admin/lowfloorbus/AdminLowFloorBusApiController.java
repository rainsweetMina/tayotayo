package kroryi.bus2.controller.admin.lowfloorbus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kroryi.bus2.dto.lowfloorbus.CreateLowFloorBusRequestDTO;
import kroryi.bus2.dto.lowfloorbus.LowFloorBusResponseDTO;
import kroryi.bus2.dto.lowfloorbus.UpdateLowFloorBusRequestDTO;
import kroryi.bus2.service.admin.lowfloorbus.LowFloorBusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Tag(name = "관리자-저상버스 대체 안내", description = "저상버스 대체 안내 관리 API")
@RestController
@RequestMapping("/api/admin/lowfloorbuses")
@RequiredArgsConstructor
@Log4j2
public class AdminLowFloorBusApiController {

    private final LowFloorBusService lowFloorBusService;

    @Operation(summary = "저상버스 대체 안내 전체 목록")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LowFloorBusResponseDTO>> getAllLowFloorBuses() {
        log.info("🔴 ADMIN API 호출됨 - /api/admin/lowfloorbuses");
        return ResponseEntity.ok(lowFloorBusService.getAllLowFloorBuses());
    }

    @Operation(summary = "저상버스 대체 안내 상세 조회")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LowFloorBusResponseDTO> getLowFloorBus(@PathVariable Long id) {
        log.info("🔴 ADMIN API 호출됨 - /api/admin/lowfloorbuses/{}", id);
        LowFloorBusResponseDTO lowFloorBus = lowFloorBusService.getLowFloorBusById(id);
        log.info("🔴 저상버스 대체 안내 조회 완료 - ID: {}, 제목: {}, 조회수: {}", id, lowFloorBus.getTitle(), lowFloorBus.getViewCount());
        return ResponseEntity.ok(lowFloorBus);
    }

    @Operation(summary = "저상버스 대체 안내 등록")
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LowFloorBusResponseDTO> createLowFloorBus(
            @RequestPart("lowFloorBus") @Valid CreateLowFloorBusRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            log.info("📨 저상버스 대체 안내 등록 요청: {}", dto);
            
            if (files != null && !files.isEmpty()) {
                log.info("📨 첨부파일 정보: 개수={}", files.size());
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    log.info("📨 파일[{}]: 이름={}, 크기={}, 타입={}", 
                        i, file.getOriginalFilename(), file.getSize(), file.getContentType());
                }
            } else {
                log.info("📨 첨부파일 없음");
            }
            
            LowFloorBusResponseDTO created = lowFloorBusService.createLowFloorBus(dto, files);
            log.info("📨 저상버스 대체 안내 등록 성공: ID={}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("📨 저상버스 대체 안내 등록 실패", e);
            throw e;
        }
    }

    @Operation(summary = "저상버스 대체 안내 수정")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LowFloorBusResponseDTO> updateLowFloorBus(
            @PathVariable Long id,
            @RequestPart(value = "lowFloorBus", required = true) @Valid UpdateLowFloorBusRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(lowFloorBusService.updateLowFloorBus(id, dto, files));
    }

    @Operation(summary = "저상버스 대체 안내 탑공지 설정/해제")
    @PatchMapping(value = "/{id}/top-notice")
    public ResponseEntity<LowFloorBusResponseDTO> toggleTopNotice(
            @PathVariable Long id,
            @RequestParam boolean topNotice
    ) {
        log.info("🔴 ADMIN API 호출됨 - 저상버스 대체 안내 탑공지 {} 설정: {}", 
                topNotice ? "활성화" : "비활성화", id);
        LowFloorBusResponseDTO updated = lowFloorBusService.toggleTopNotice(id, topNotice);
        log.info("🔴 저상버스 대체 안내 탑공지 설정 완료 - ID: {}, 제목: {}, 탑공지: {}", 
                id, updated.getTitle(), updated.isTopNotice());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "저상버스 대체 안내 삭제")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteLowFloorBus(@PathVariable Long id) {
        lowFloorBusService.deleteLowFloorBus(id);
        return ResponseEntity.noContent().build();
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
} 