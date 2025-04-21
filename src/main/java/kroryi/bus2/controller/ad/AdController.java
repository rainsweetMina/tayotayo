package kroryi.bus2.controller.ad;

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

@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final AdCompanyRepository adCompanyRepository;

    // ✅ 광고 등록 - FormData 방식 (JSON DTO + 이미지 파일)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> createAd(
            @RequestPart("dto") AdRequestDTO dto,
            @RequestPart("image") MultipartFile imageFile
    ) {
        Ad savedAd = adService.saveAdWithImage(dto, imageFile);
        return ResponseEntity.ok(savedAd);
    }

    // ✅ 광고 수정 - FormData 방식 (JSON DTO + 이미지 파일)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> updateAdWithImage(
            @PathVariable Long id,
            @RequestPart("dto") AdUpdateRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        Ad updatedAd = adService.updateAdWithImage(id, dto, imageFile);
        return ResponseEntity.ok(updatedAd);
    }

    @GetMapping
    public ResponseEntity<List<AdResponseDTO>> getAllAds() {
        return ResponseEntity.ok(adService.getAllAds());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AdResponseDTO>> getActiveAds() {
        return ResponseEntity.ok(adService.getActiveAds());
    }

    @GetMapping("/stats")
    public ResponseEntity<AdStatsDTO> getAdStats() {
        return ResponseEntity.ok(adService.getAdStats());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ended")
    public ResponseEntity<List<AdResponseDTO>> getEndedAds() {
        return ResponseEntity.ok(adService.getEndedAds());
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<AdResponseDTO>> getDeletedAds() {
        return ResponseEntity.ok(adService.getDeletedAds());
    }

    @GetMapping("/companies")
    public ResponseEntity<List<AdCompany>> getAllCompanies() {
        return ResponseEntity.ok(adCompanyRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdResponseDTO> getAdById(@PathVariable Long id) {
        return ResponseEntity.ok(adService.getAdById(id));
    }

    // ✅ 광고 팝업용 API - 유효한 광고 하나만 전달
    @GetMapping("/popup")
    public ResponseEntity<AdPopupResponseDTO> getPopupAd() {
        return adService.findPopupAd()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}