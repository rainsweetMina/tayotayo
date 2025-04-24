package kroryi.bus2.service.ad;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.ad.*;
import kroryi.bus2.entity.ad.Ad;
import kroryi.bus2.entity.ad.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.repository.jpa.AdRepository;
import kroryi.bus2.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final AdCompanyRepository adCompanyRepository;
    private final FileUploadUtil fileUploadUtil; // ✅ 이미지 저장 유틸 주입

    // ✅ FormData 기반 광고 등록 메서드 추가됨
    @AdminAudit(action = "광고 등록 (파일업로드)", target = "Ad")
    public Ad saveAdWithImage(AdRequestDTO dto, MultipartFile imageFile) {
        AdCompany company = adCompanyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("광고회사 정보를 찾을 수 없습니다."));

        String fullPath = fileUploadUtil.saveAdImage(imageFile);
        String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1); // ✅ 파일명만 저장

        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .imageUrl(fileName)
                .linkUrl(dto.getLinkUrl())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .showPopup(dto.isShowPopup())
                .deleted(false)
                .company(company)
                .build();

        return adRepository.save(ad);
    }

    public List<AdResponseDTO> getAllAds() {
        return adRepository.findAll().stream()
                .filter(ad -> !ad.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AdResponseDTO> getActiveAds() {
        LocalDateTime now = LocalDateTime.now();
        return adRepository.findAll().stream()
                .filter(ad -> !ad.isDeleted() &&
                        ad.getStartDateTime().isBefore(now) &&
                        ad.getEndDateTime().isAfter(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AdStatsDTO getAdStats() {
        List<Ad> ads = adRepository.findAll();

        long scheduled = ads.stream().filter(ad -> !ad.isDeleted() && ad.getStatus().equals("SCHEDULED")).count();
        long ongoing = ads.stream().filter(ad -> !ad.isDeleted() && ad.getStatus().equals("ONGOING")).count();
        long endingSoon = ads.stream().filter(ad -> !ad.isDeleted() && ad.getStatus().equals("ENDING_SOON")).count();
        long ended = ads.stream().filter(ad -> !ad.isDeleted() && ad.getStatus().equals("ENDED")).count();
        long deleted = ads.stream().filter(Ad::isDeleted).count();

        return new AdStatsDTO(scheduled, ongoing, endingSoon, ended, deleted);
    }

    @AdminAudit(action = "광고 삭제", target = "Ad")
    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고 ID를 찾을 수 없습니다."));
        ad.setDeleted(true);
        adRepository.save(ad);
    }

    public List<AdResponseDTO> getEndedAds() {
        LocalDateTime now = LocalDateTime.now();
        return adRepository.findAll().stream()
                .filter(ad -> !ad.isDeleted() && ad.getEndDateTime().isBefore(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AdResponseDTO> getDeletedAds() {
        return adRepository.findAll().stream()
                .filter(Ad::isDeleted)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @AdminAudit(action = "광고 수정 (파일업로드)", target = "Ad")
    public Ad updateAdWithImage(Long id, AdUpdateRequestDTO dto, MultipartFile imageFile) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다."));

        // ✅ 연장일 경우 extensionCount 증가
        if (dto.getEndDateTime().isAfter(ad.getEndDateTime())) {
            ad.setExtensionCount(ad.getExtensionCount() + 1);
        }

        ad.setTitle(dto.getTitle());
        ad.setLinkUrl(dto.getLinkUrl());
        ad.setStartDateTime(dto.getStartDateTime());
        ad.setEndDateTime(dto.getEndDateTime());
        ad.setShowPopup(dto.isShowPopup());

        if (dto.getCompanyId() != null) {
            AdCompany company = adCompanyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("광고회사 정보를 찾을 수 없습니다."));
            ad.setCompany(company);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String fullPath = fileUploadUtil.saveAdImage(imageFile);
            String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1); // ✅ 파일명만 저장
            ad.setImageUrl(fileName);
        }

        return adRepository.save(ad);
    }


    public AdResponseDTO getAdById(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 광고가 존재하지 않습니다."));
        return convertToDTO(ad);
    }

    private AdResponseDTO convertToDTO(Ad ad) {
        AdCompany company = ad.getCompany();

        return AdResponseDTO.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .imageUrl(ad.getImageUrl())
                .linkUrl(ad.getLinkUrl())
                .startDateTime(ad.getStartDateTime())
                .endDateTime(ad.getEndDateTime())
                .status(ad.getStatus() != null ? ad.getStatus() : "UNKNOWN")
                .extensionCount(ad.getExtensionCount()) // 광고연장횟수
                .company(company != null ? AdCompanyDTO.builder()
                        .id(company.getId())
                        .name(company.getName())
                        .managerName(company.getManagerName())
                        .contactNumber(company.getContactNumber())
                        .email(company.getEmail())
                        .build() : null)
                .companyName(company != null ? company.getName() : null)
                .managerName(company != null ? company.getManagerName() : null)
                .contactNumber(company != null ? company.getContactNumber() : null)
                .email(company != null ? company.getEmail() : null)
                .build();
    }

    // AdServiceImpl.java
    public Optional<Ad> findValidPopupAd() {
        LocalDateTime now = LocalDateTime.now();
        return adRepository.findFirstByDeletedFalseAndStartDateTimeBeforeAndEndDateTimeAfterOrderByStartDateTimeDesc(now, now);
    }

    public Optional<AdPopupResponseDTO> findPopupAd() {
        LocalDateTime now = LocalDateTime.now();
        return adRepository
                .findFirstByDeletedFalseAndStartDateTimeBeforeAndEndDateTimeAfterOrderByStartDateTimeDesc(now, now)
                .map(AdPopupResponseDTO::new);
    }

}
