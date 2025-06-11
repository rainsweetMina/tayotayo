package kroryi.bus2.controller.admin.monitoring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "관리자 로그", description = "")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogServiceImpl auditLogServiceImpl;

    // /api/admin/logs?page=0&size=10
    @Operation(summary = "로그 조회")
    @GetMapping
    public Page<AdminAuditLog> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return auditLogServiceImpl.getLogs(PageRequest.of(page, size));
    }
    
    @Operation(summary = "로그 다운로드 (Excel)")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadLogs() throws IOException {
        // 페이징 없이 모든 로그 가져오기 (최대 10,000개로 제한)
        Page<AdminAuditLog> logs = auditLogServiceImpl.getLogs(PageRequest.of(0, 10000));
        
        // Excel 워크북 생성
        try (Workbook workbook = new XSSFWorkbook()) {
            // 시트 생성
            Sheet sheet = workbook.createSheet("시스템 로그");
            
            // 헤더 스타일 설정
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 헤더 행 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "관리자ID", "작업", "대상", "작업 시간", "이전 값", "변경 값"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // 열 너비 설정
            }
            
            // 로그 데이터 추가
            int rowNum = 1;
            for (AdminAuditLog log : logs.getContent()) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(log.getId() != null ? log.getId() : 0);
                row.createCell(1).setCellValue(log.getAdminId() != null ? log.getAdminId() : "");
                row.createCell(2).setCellValue(log.getAction() != null ? log.getAction() : "");
                row.createCell(3).setCellValue(log.getTarget() != null ? log.getTarget() : "");
                row.createCell(4).setCellValue(log.getTimestamp() != null ? 
                        log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                
                // 이전 값과 변경 값은 최대 길이 제한 (너무 길면 엑셀 셀에 표시하기 어려움)
                String beforeValue = log.getBeforeValue() != null ? log.getBeforeValue() : "";
                String afterValue = log.getAfterValue() != null ? log.getAfterValue() : "";
                
                if (beforeValue.length() > 500) {
                    beforeValue = beforeValue.substring(0, 497) + "...";
                }
                
                if (afterValue.length() > 500) {
                    afterValue = afterValue.substring(0, 497) + "...";
                }
                
                row.createCell(5).setCellValue(beforeValue);
                row.createCell(6).setCellValue(afterValue);
            }
            
            // 엑셀 파일 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            String fileName = "system_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            HttpHeaders httpHeaders = new HttpHeaders();
            // Excel 파일에 맞는 MIME 타입 설정
            httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 브라우저에게 파일 다운로드임을 명시
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            // 캐시 설정
            httpHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(outputStream.toByteArray());
        }
    }
    
    @Operation(summary = "로그 ID로 조회")
    @GetMapping("/id/{id}")
    public ResponseEntity<AdminAuditLog> getLogById(@PathVariable Long id) {
        return auditLogServiceImpl.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}