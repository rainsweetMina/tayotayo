package kroryi.bus2.service.ad;


import kroryi.bus2.dto.ad.AdCompanyDropdownDTO;
import kroryi.bus2.dto.ad.AdCompanyRequestDTO;
import kroryi.bus2.dto.ad.AdCompanyUpdateRequestDTO;
import kroryi.bus2.entity.ad.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdCompanyService {

    private final AdCompanyRepository adCompanyRepository;

    public List<AdCompany> getAllCompanies() {
        return adCompanyRepository.findByDeletedFalse();  // 🔁 전체 조회 대신 삭제 안 된 것만
    }


    public AdCompany registerCompany(AdCompanyRequestDTO dto) {
        AdCompany company = AdCompany.builder()
                .name(dto.getName())
                .managerName(dto.getManagerName())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .build();

        return adCompanyRepository.save(company);
    }
    public AdCompany updateCompany(Long id, AdCompanyUpdateRequestDTO dto) {
        AdCompany company = adCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고회사 정보를 찾을 수 없습니다."));

        company.setName(dto.getName());
        company.setManagerName(dto.getManagerName());
        company.setContactNumber(dto.getContactNumber());
        company.setEmail(dto.getEmail());

        return adCompanyRepository.save(company);
    }
    public List<AdCompanyDropdownDTO> getCompanyDropdownList() {
        return adCompanyRepository.findByDeletedFalse()
                .stream()
                .map(company -> new AdCompanyDropdownDTO(company.getId(), company.getName()))
                .collect(Collectors.toList());
    }
    public void softDeleteCompany(Long id) {
        AdCompany company = adCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고회사를 찾을 수 없습니다."));

        company.setDeleted(true);
        adCompanyRepository.save(company); // 삭제 대신 상태만 바꿈
    }



}
