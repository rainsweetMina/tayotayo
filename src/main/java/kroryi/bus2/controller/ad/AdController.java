package kroryi.bus2.controller.ad;

import kroryi.bus2.dto.ad.*;
import kroryi.bus2.entity.ad.Ad;
import kroryi.bus2.entity.ad.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.service.ad.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final AdCompanyRepository adCompanyRepository; // ✅ 이 줄이 필요함


    @PostMapping
    public ResponseEntity<Ad> createAd(@RequestBody AdRequestDTO dto) {
        Ad savedAd = adService.saveAd(dto);
        return ResponseEntity.ok(savedAd);
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
    @PutMapping("/{id}")
    public ResponseEntity<Ad> updateAd(@PathVariable Long id, @RequestBody AdUpdateRequestDTO dto) {
        Ad updatedAd = adService.updateAd(id, dto);
        return ResponseEntity.ok(updatedAd);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AdResponseDTO> getAdById(@PathVariable Long id) {
        return ResponseEntity.ok(adService.getAdById(id));
    }
    // PublicAdController.java
    @GetMapping("/popup")
    public ResponseEntity<AdPopupResponseDTO> getPopupAd() {
        return adService.findPopupAd()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }







}
