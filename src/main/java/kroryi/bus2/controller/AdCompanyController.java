package kroryi.bus2.controller;

import kroryi.bus2.dto.ad.AdCompanyDropdownDTO;
import kroryi.bus2.dto.ad.AdCompanyRequestDTO;
import kroryi.bus2.dto.ad.AdCompanyUpdateRequestDTO;
import kroryi.bus2.entity.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.service.ad.AdCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ad-company")
public class AdCompanyController {

    private final AdCompanyService adCompanyService;
    private final AdCompanyRepository adCompanyRepository;

    // 광고회사 등록
    @PostMapping
    public ResponseEntity<AdCompany> register(@RequestBody AdCompanyRequestDTO dto) {
        AdCompany created = adCompanyService.registerCompany(dto);
        return ResponseEntity.ok(created);
    }

    // 광고회사 수정
    @PutMapping("/{id}")
    public ResponseEntity<AdCompany> updateCompany(
            @PathVariable Long id,
            @RequestBody AdCompanyUpdateRequestDTO dto
    ) {
        AdCompany updatedCompany = adCompanyService.updateCompany(id, dto);
        return ResponseEntity.ok(updatedCompany);
    }

    // 광고회사 드롭다운용 목록 조회 (id, name만 반환)
    @GetMapping
    public List<AdCompanyDropdownDTO> getAllCompanies() {
        return adCompanyRepository.findAll().stream()
                .map(company -> new AdCompanyDropdownDTO(company.getId(), company.getName()))
                .collect(Collectors.toList());
    }
    @GetMapping("/dropdown")
    public ResponseEntity<List<AdCompanyDropdownDTO>> getDropdownCompanies() {
        return ResponseEntity.ok(adCompanyService.getCompanyDropdownList());
    }

}
