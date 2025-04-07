package kroryi.bus2.service.ad;


import kroryi.bus2.dto.ad.AdCompanyDropdownDTO;
import kroryi.bus2.dto.ad.AdCompanyRequestDTO;
import kroryi.bus2.dto.ad.AdCompanyUpdateRequestDTO;
import kroryi.bus2.entity.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdCompanyService {

    private final AdCompanyRepository adCompanyRepository;

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
        return adCompanyRepository.findAll().stream()
                .map(company -> new AdCompanyDropdownDTO(company.getId(), company.getName()))
                .toList();
    }

}
