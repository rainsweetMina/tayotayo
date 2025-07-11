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
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "광고-관리", description = "광고 등록, 수정, 조회, 삭제 API")
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
@Log4j2
public class AdController {

    private final AdService adService;
    private final AdCompanyRepository adCompanyRepository;

    // ✅ 광고 등록 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 등록", description = "FormData 방식으로 광고 정보를 등록합니다. 이미지 파일과 JSON DTO를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "광고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 유효성 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAd(
            @RequestPart("dto") AdRequestDTO dto,
            @RequestPart("image") MultipartFile imageFile
    ) {
        try {
            log.info("광고 등록 요청 시작");
            log.info("DTO 정보: title={}, linkUrl={}, startDateTime={}, endDateTime={}, companyId={}, showPopup={}", 
                dto.getTitle(), dto.getLinkUrl(), dto.getStartDateTime(), dto.getEndDateTime(), dto.getCompanyId(), dto.isShowPopup());
            log.info("이미지 파일 정보: filename={}, size={}, contentType={}", 
                imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());
            
            // 파일 유효성 검사
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 필요합니다."));
            }

            // 파일 크기 검사 (20MB 제한)
            if (imageFile.getSize() > 20 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 20MB를 초과할 수 없습니다."));
            }

            // 파일 타입 검사 (더 유연하게)
            String contentType = imageFile.getContentType();
            if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/octet-stream")) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            log.info("파일 유효성 검사 통과, 서비스 호출 시작");
            Ad savedAd = adService.saveAdWithImage(dto, imageFile);
            log.info("광고 등록 성공: ID={}, 제목={}", savedAd.getId(), savedAd.getTitle());
            
            return ResponseEntity.ok(savedAd);
            
        } catch (Exception e) {
            log.error("광고 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 등록에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<AdResponseDTO> extendAd(
            @PathVariable Long id,
            @RequestBody AdExtensionRequestDTO extendDTO
    ) {
        Ad extendedAd = adService.extendAd(id, extendDTO.getNewEndDateTime());
        AdResponseDTO dto = adService.getAdById(extendedAd.getId()); // 이미 DTO 변환 메서드 있음
        return ResponseEntity.ok(dto);
    }



    // ✅ 광고 수정 - FormData 방식 (JSON DTO + 이미지 파일)
    @Operation(summary = "광고 수정", description = "기존 광고 정보를 수정합니다. 이미지 파일은 선택적으로 포함할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 광고를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdWithImage(
            @PathVariable Long id,
            @RequestPart("dto") AdUpdateRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            // 이미지 파일이 제공된 경우 유효성 검사
            if (imageFile != null && !imageFile.isEmpty()) {
                // 파일 크기 검사 (20MB 제한)
                if (imageFile.getSize() > 20 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 20MB를 초과할 수 없습니다."));
                }

                // 파일 타입 검사 (더 유연하게)
                String contentType = imageFile.getContentType();
                if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/octet-stream")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
                }
            }

            Ad updatedAd = adService.updateAdWithImage(id, dto, imageFile);
            log.info("광고 수정 성공: ID={}, 제목={}", updatedAd.getId(), updatedAd.getTitle());
            
            return ResponseEntity.ok(updatedAd);
            
        } catch (IllegalArgumentException e) {
            log.warn("광고 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 수정에 실패했습니다: " + e.getMessage()));
        }
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdStatsDTO> getAdStats() {
        return ResponseEntity.ok(adService.getAdStats());
    }

    @Operation(summary = "광고 삭제", description = "선택한 광고를 소프트 삭제 처리합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        try {
            adService.deleteAd(id);
            log.info("광고 삭제 성공: ID={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("광고 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "종료된 광고 조회", description = "종료일이 지난 광고를 조회합니다.")
    @GetMapping("/ended")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdResponseDTO>> getEndedAds() {
        return ResponseEntity.ok(adService.getEndedAds());
    }

    @Operation(summary = "삭제된 광고 조회", description = "소프트 삭제 처리된 광고 목록을 조회합니다.")
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<?> getAdById(@PathVariable Long id) {
        try {
            AdResponseDTO ad = adService.getAdById(id);
            return ResponseEntity.ok(ad);
        } catch (IllegalArgumentException e) {
            log.warn("광고 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("광고 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "광고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // ✅ 광고 팝업용 API - 유효한 광고 하나만 전달
    @Operation(summary = "팝업 광고 조회", description = "팝업으로 표시할 유효한 광고 하나를 반환합니다.")
    @GetMapping("/popup")
    public ResponseEntity<AdPopupResponseDTO> getPopupAd() {
        return adService.findPopupAd()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ✅ 파일 업로드 테스트용 엔드포인트 (개발용)
    @Operation(summary = "파일 업로드 테스트", description = "파일 업로드 기능을 테스트합니다. (개발용)")
    @PostMapping(value = "/test-upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 정보 로깅
            log.info("업로드된 파일 정보:");
            log.info("- 파일명: {}", file.getOriginalFilename());
            log.info("- 파일 크기: {} bytes", file.getSize());
            log.info("- Content-Type: {}", file.getContentType());
            
            return ResponseEntity.ok(Map.of(
                "message", "파일 업로드 테스트 성공",
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "contentType", file.getContentType()
            ));
            
        } catch (Exception e) {
            log.error("파일 업로드 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "파일 업로드 테스트에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "광고 데이터 엑셀 다운로드", description = "광고 데이터를 엑셀 파일로 다운로드합니다.")
    @PostMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadAdsAsExcel(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ads = (List<Map<String, Object>>) request.get("ads");
            String filter = (String) request.get("filter");
            String searchKeyword = (String) request.get("searchKeyword");
            
            if (ads == null || ads.isEmpty()) {
                return ResponseEntity.badRequest().body("다운로드할 광고 데이터가 없습니다.".getBytes());
            }
            
            // Excel 워크북 생성
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("광고 목록");
                
                // 헤더 스타일 생성
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                
                // 헤더 생성
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "제목", "광고회사", "상태", "연장횟수", "시작일", "종료일", "링크 URL", "이미지 URL"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                    sheet.setColumnWidth(i, 4000); // 컬럼 너비 설정
                }
                
                // 데이터 행 생성
                int rowNum = 1;
                for (Map<String, Object> ad : ads) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(ad.get("id") != null ? ad.get("id").toString() : "");
                    row.createCell(1).setCellValue(ad.get("title") != null ? ad.get("title").toString() : "");
                    row.createCell(2).setCellValue(ad.get("companyName") != null ? ad.get("companyName").toString() : "");
                    
                    // 상태 한글 변환
                    String status = ad.get("status") != null ? ad.get("status").toString() : "";
                    String statusText = switch (status) {
                        case "SCHEDULED" -> "예정";
                        case "ONGOING" -> "진행중";
                        case "ENDING_SOON" -> "종료임박";
                        case "ENDED" -> "종료됨";
                        case "DELETED" -> "삭제됨";
                        default -> status;
                    };
                    row.createCell(3).setCellValue(statusText);
                    
                    row.createCell(4).setCellValue(ad.get("extensionCount") != null ? ad.get("extensionCount").toString() : "0");
                    row.createCell(5).setCellValue(ad.get("startDateTime") != null ? ad.get("startDateTime").toString() : "");
                    row.createCell(6).setCellValue(ad.get("endDateTime") != null ? ad.get("endDateTime").toString() : "");
                    row.createCell(7).setCellValue(ad.get("linkUrl") != null ? ad.get("linkUrl").toString() : "");
                    row.createCell(8).setCellValue(ad.get("imageUrl") != null ? ad.get("imageUrl").toString() : "");
                }
                
                // 파일명 생성
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filterText = switch (filter) {
                    case "all" -> "전체";
                    case "scheduled" -> "예정";
                    case "ongoing" -> "진행중";
                    case "ending_soon" -> "종료임박";
                    case "ended" -> "종료됨";
                    default -> "전체";
                };
                String filename = String.format("광고목록_%s_%s.xlsx", filterText, now);
                
                // Excel 파일을 바이트 배열로 변환
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                byte[] excelBytes = outputStream.toByteArray();
                
                // HTTP 헤더 설정
                HttpHeaders headers_response = new HttpHeaders();
                headers_response.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers_response.setContentDispositionFormData("attachment", filename);
                headers_response.setContentLength(excelBytes.length);
                
                log.info("광고 데이터 엑셀 다운로드 성공: {}개 항목, 파일명: {}", ads.size(), filename);
                
                return ResponseEntity.ok()
                    .headers(headers_response)
                    .body(excelBytes);
                    
            } catch (IOException e) {
                log.error("Excel 파일 생성 실패: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError().body("Excel 파일 생성에 실패했습니다.".getBytes());
            }
            
        } catch (Exception e) {
            log.error("광고 데이터 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("광고 데이터 다운로드에 실패했습니다.".getBytes());
        }
    }

    @Operation(summary = "광고 회사 데이터 엑셀 다운로드", description = "광고 회사 데이터를 엑셀 파일로 다운로드합니다.")
    @PostMapping("/companies/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadAdCompaniesAsExcel(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> companies = (List<Map<String, Object>>) request.get("companies");
            String searchKeyword = (String) request.get("searchKeyword");
            
            if (companies == null || companies.isEmpty()) {
                return ResponseEntity.badRequest().body("다운로드할 광고 회사 데이터가 없습니다.".getBytes());
            }
            
            // Excel 워크북 생성
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("광고 회사 목록");
                
                // 헤더 스타일 생성
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                
                // 헤더 생성
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "회사명", "담당자명", "연락처", "이메일"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                    sheet.setColumnWidth(i, 4000); // 컬럼 너비 설정
                }
                
                // 데이터 행 생성
                int rowNum = 1;
                for (Map<String, Object> company : companies) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(company.get("id") != null ? company.get("id").toString() : "");
                    row.createCell(1).setCellValue(company.get("name") != null ? company.get("name").toString() : "");
                    row.createCell(2).setCellValue(company.get("managerName") != null ? company.get("managerName").toString() : "");
                    row.createCell(3).setCellValue(company.get("contactNumber") != null ? company.get("contactNumber").toString() : "");
                    row.createCell(4).setCellValue(company.get("email") != null ? company.get("email").toString() : "");
                }
                
                // 파일명 생성
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String searchText = searchKeyword != null && !searchKeyword.isEmpty() ? "_" + searchKeyword : "";
                String filename = String.format("광고회사목록%s_%s.xlsx", searchText, now);
                
                // Excel 파일을 바이트 배열로 변환
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                byte[] excelBytes = outputStream.toByteArray();
                
                // HTTP 헤더 설정
                HttpHeaders headers_response = new HttpHeaders();
                headers_response.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers_response.setContentDispositionFormData("attachment", filename);
                headers_response.setContentLength(excelBytes.length);
                
                log.info("광고 회사 데이터 엑셀 다운로드 성공: {}개 항목, 파일명: {}", companies.size(), filename);
                
                return ResponseEntity.ok()
                    .headers(headers_response)
                    .body(excelBytes);
                    
            } catch (IOException e) {
                log.error("Excel 파일 생성 실패: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError().body("Excel 파일 생성에 실패했습니다.".getBytes());
            }
            
        } catch (Exception e) {
            log.error("광고 회사 데이터 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("광고 회사 데이터 다운로드에 실패했습니다.".getBytes());
        }
    }
}