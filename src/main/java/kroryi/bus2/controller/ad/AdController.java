package kroryi.bus2.controller.ad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.ad.*;
import kroryi.bus2.entity.ad.Ad;
import kroryi.bus2.entity.ad.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.service.ad.AdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "광고-관리", description = "광고 등록, 수정, 조회, 삭제 API")
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
@Log4j2
public class AdController {

    private final AdService adService;
    private final AdCompanyRepository adCompanyRepository;

    // ✅ 광고 등록 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 등록", description = "FormData 방식으로 광고 정보를 등록합니다. 이미지 파일과 JSON DTO를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "광고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 유효성 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAd(
            @RequestPart("dto") AdRequestDTO dto,
            @RequestPart("image") MultipartFile imageFile
    ) {
        try {
            // 파일 유효성 검사
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 필요합니다."));
            }

            // 파일 크기 검사 (20MB 제한)
            if (imageFile.getSize() > 20 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 20MB를 초과할 수 없습니다."));
            }

            // 파일 타입 검사 (더 유연하게)
            String contentType = imageFile.getContentType();
            if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/octet-stream")) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            Ad savedAd = adService.saveAdWithImage(dto, imageFile);
            log.info("광고 등록 성공: ID={}, 제목={}", savedAd.getId(), savedAd.getTitle());
            
            return ResponseEntity.ok(savedAd);
            
        } catch (Exception e) {
            log.error("광고 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 등록에 실패했습니다: " + e.getMessage()));
        }
    }

    // ✅ 광고 수정 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 수정", description = "기존 광고 정보를 수정합니다. 이미지 파일은 선택적으로 포함할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 광고를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdWithImage(
            @PathVariable Long id,
            @RequestPart("dto") AdUpdateRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            // 이미지 파일이 제공된 경우 유효성 검사
            if (imageFile != null && !imageFile.isEmpty()) {
                // 파일 크기 검사 (20MB 제한)
                if (imageFile.getSize() > 20 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 20MB를 초과할 수 없습니다."));
                }

                // 파일 타입 검사 (더 유연하게)
                String contentType = imageFile.getContentType();
                if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/octet-stream")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
                }
            }

            Ad updatedAd = adService.updateAdWithImage(id, dto, imageFile);
            log.info("광고 수정 성공: ID={}, 제목={}", updatedAd.getId(), updatedAd.getTitle());
            
            return ResponseEntity.ok(updatedAd);
            
        } catch (IllegalArgumentException e) {
            log.warn("광고 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "광고 전체 조회", description = "등록된 모든 광고를 반환합니다.")
    @GetMapping
    public ResponseEntity<List<AdResponseDTO>> getAllAds() {
        return ResponseEntity.ok(adService.getAllAds());
    }

    @Operation(summary = "진행 중 광고 조회", description = "현재 노출 중인 광고를 조회합니다.")
    @GetMapping("/active")
    public ResponseEntity<List<AdResponseDTO>> getActiveAds() {
        return ResponseEntity.ok(adService.getActiveAds());
    }

    @Operation(summary = "광고 통계 조회", description = "광고 등록 수, 진행 수, 종료 수 등을 통계로 반환합니다.")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdStatsDTO> getAdStats() {
        return ResponseEntity.ok(adService.getAdStats());
    }

    @Operation(summary = "광고 삭제", description = "선택한 광고를 소프트 삭제 처리합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        try {
            adService.deleteAd(id);
            log.info("광고 삭제 성공: ID={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("광고 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "종료된 광고 조회", description = "종료일이 지난 광고를 조회합니다.")
    @GetMapping("/ended")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdResponseDTO>> getEndedAds() {
        return ResponseEntity.ok(adService.getEndedAds());
    }

    @Operation(summary = "삭제된 광고 조회", description = "소프트 삭제 처리된 광고 목록을 조회합니다.")
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdResponseDTO>> getDeletedAds() {
        return ResponseEntity.ok(adService.getDeletedAds());
    }

    @Operation(summary = "광고회사 전체 조회", description = "모든 광고회사 정보를 반환합니다.")
    @GetMapping("/companies")
    public ResponseEntity<List<AdCompany>> getAllCompanies() {
        return ResponseEntity.ok(adCompanyRepository.findAll());
    }

    @Operation(summary = "광고 ID로 조회", description = "ID로 광고 정보를 단건 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdById(@PathVariable Long id) {
        try {
            AdResponseDTO ad = adService.getAdById(id);
            return ResponseEntity.ok(ad);
        } catch (IllegalArgumentException e) {
            log.warn("광고 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // ✅ 광고 팝업용 API - 유효한 광고 하나만 전달
    @Operation(summary = "팝업 광고 조회", description = "팝업으로 표시할 유효한 광고 하나를 반환합니다.")
    @GetMapping("/popup")
    public ResponseEntity<AdPopupResponseDTO> getPopupAd() {
        return adService.findPopupAd()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ✅ 파일 업로드 테스트용 엔드포인트 (개발용)
    @Operation(summary = "파일 업로드 테스트", description = "파일 업로드 기능을 테스트합니다. (개발용)")
    @PostMapping(value = "/test-upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
            }

            // 파일 정보 반환
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("originalFilename", file.getOriginalFilename());
            fileInfo.put("contentType", file.getContentType());
            fileInfo.put("size", file.getSize());
            fileInfo.put("message", "파일 업로드 테스트 성공");

            log.info("파일 업로드 테스트 성공: {}", file.getOriginalFilename());
            return ResponseEntity.ok(fileInfo);

        } catch (Exception e) {
            log.error("파일 업로드 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "파일 업로드 테스트 실패: " + e.getMessage()));
        }
    }
}