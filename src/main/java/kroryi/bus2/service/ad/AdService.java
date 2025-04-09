package kroryi.bus2.service.ad;

import kroryi.bus2.dto.ad.*;
import kroryi.bus2.entity.Ad;
import kroryi.bus2.entity.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.repository.jpa.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final AdCompanyRepository adCompanyRepository;


    public Ad saveAd(AdRequestDTO dto) {
        AdCompany company = adCompanyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("광고회사 정보를 찾을 수 없습니다."));

        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .imageUrl(dto.getImageUrl())
                .linkUrl(dto.getLinkUrl())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .deleted(false)
                .company(company) // ✅ 여기서 연동
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
    public Ad updateAd(Long id, AdUpdateRequestDTO dto) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다."));

        ad.setTitle(dto.getTitle());
        ad.setImageUrl(dto.getImageUrl());
        ad.setLinkUrl(dto.getLinkUrl());
        ad.setStartDateTime(dto.getStartDateTime());
        ad.setEndDateTime(dto.getEndDateTime());

        // 광고회사 수정도 포함시킬 경우:
        if (dto.getCompanyId() != null) {
            AdCompany company = adCompanyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("광고회사 정보를 찾을 수 없습니다."));
            ad.setCompany(company);
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
                .status(ad.getStatus())
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





}