package kroryi.bus2.controller.ad;

import io.swagger.annotations.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.ad.AdCompanyDropdownDTO;
import kroryi.bus2.dto.ad.AdCompanyRequestDTO;
import kroryi.bus2.dto.ad.AdCompanyResponseDTO;
import kroryi.bus2.dto.ad.AdCompanyUpdateRequestDTO;
import kroryi.bus2.entity.ad.AdCompany;
import kroryi.bus2.repository.jpa.AdCompanyRepository;
import kroryi.bus2.service.ad.AdCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "광고-회사-관리", description = "")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ad-company")
public class AdCompanyController {

    private final AdCompanyService adCompanyService;
    private final AdCompanyRepository adCompanyRepository;

    // 광고회사 등록
    @Operation(summary = "광고회사 등록", description = "새로운 광고회사를 등록합니다.")
    @ApiResponse(code = 200, message = "등록 성공")
    @PostMapping
    public ResponseEntity<AdCompany> register(@RequestBody AdCompanyRequestDTO dto) {
        AdCompany created = adCompanyService.registerCompany(dto);
        return ResponseEntity.ok(created);
    }

    // 광고회사 수정
    @Operation(summary = "광고회사 수정", description = "광고회사 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<AdCompany> updateCompany(
            @PathVariable Long id,
            @RequestBody AdCompanyUpdateRequestDTO dto
    ) {
        AdCompany updatedCompany = adCompanyService.updateCompany(id, dto);
        return ResponseEntity.ok(updatedCompany);
    }

    // 광고회사 드롭다운용 목록 조회 (id, name만 반환)
    @Operation(summary = "광고회사 목록 조회", description = "삭제되지 않은 광고회사 목록을 반환합니다.")
    @GetMapping
    public List<AdCompanyResponseDTO> getAllCompanies() {
        return adCompanyRepository.findByDeletedFalse()
                .stream()
                .map(company -> AdCompanyResponseDTO.builder()
                        .id(company.getId())
                        .name(company.getName())
                        .managerName(company.getManagerName())
                        .contactNumber(company.getContactNumber())
                        .email(company.getEmail())
                        .build())
                .toList();
    }

    @Operation(summary = "광고회사 드롭다운용 목록 조회", description = "간략화된 광고회사 정보를 반환합니다 (id, name).")
    @GetMapping("/dropdown")
    public ResponseEntity<List<AdCompanyDropdownDTO>> getDropdownCompanies() {
        return ResponseEntity.ok(adCompanyService.getCompanyDropdownList());
    }

    @Operation(summary = "광고회사 삭제", description = "광고회사 정보를 삭제(소프트 삭제)합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdCompany(@PathVariable Long id) {
        adCompanyService.softDeleteCompany(id);
        return ResponseEntity.noContent().build();
    }




}
