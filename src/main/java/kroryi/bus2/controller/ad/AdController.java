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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "광고-관리", description = "")
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {


    private final AdService adService;
    private final AdCompanyRepository adCompanyRepository;

    // ✅ 광고 등록 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 등록", description = "FormData 방식으로 광고 정보를 등록합니다. 이미지 파일과 JSON DTO를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "광고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 유효성 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> createAd(
            @RequestPart("dto") AdRequestDTO dto,
            @RequestPart("image") MultipartFile imageFile
    ) {
        Ad savedAd = adService.saveAdWithImage(dto, imageFile);
        return ResponseEntity.ok(savedAd);
    }

    // ✅ 광고 수정 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 수정", description = "기존 광고 정보를 수정합니다. 이미지 파일은 선택적으로 포함할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 광고를 찾을 수 없음")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> updateAdWithImage(
            @PathVariable Long id,
            @RequestPart("dto") AdUpdateRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        Ad updatedAd = adService.updateAdWithImage(id, dto, imageFile);
        return ResponseEntity.ok(updatedAd);
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
    public ResponseEntity<AdStatsDTO> getAdStats() {
        return ResponseEntity.ok(adService.getAdStats());
    }

    @Operation(summary = "광고 삭제", description = "선택한 광고를 소프트 삭제 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "종료된 광고 조회", description = "종료일이 지난 광고를 조회합니다.")
    @GetMapping("/ended")
    public ResponseEntity<List<AdResponseDTO>> getEndedAds() {
        return ResponseEntity.ok(adService.getEndedAds());
    }

    @Operation(summary = "삭제된 광고 조회", description = "소프트 삭제 처리된 광고 목록을 조회합니다.")
    @GetMapping("/deleted")
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
    public ResponseEntity<AdResponseDTO> getAdById(@PathVariable Long id) {
        return ResponseEntity.ok(adService.getAdById(id));
    }

    // ✅ 광고 팝업용 API - 유효한 광고 하나만 전달
    @Operation(summary = "팝업 광고 조회", description = "팝업으로 표시할 유효한 광고 하나를 반환합니다.")
    @GetMapping("/popup")
    public ResponseEntity<AdPopupResponseDTO> getPopupAd() {
        return adService.findPopupAd()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}