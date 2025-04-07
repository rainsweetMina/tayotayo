package kroryi.bus2.controller;

import kroryi.bus2.dto.ad.AdRequestDTO;
import kroryi.bus2.dto.ad.AdResponseDTO;
import kroryi.bus2.dto.ad.AdStatsDTO;
import kroryi.bus2.dto.ad.AdUpdateRequestDTO;
import kroryi.bus2.entity.Ad;
import kroryi.bus2.entity.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.service.ad.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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





}
