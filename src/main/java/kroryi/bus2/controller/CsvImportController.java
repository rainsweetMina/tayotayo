package kroryi.bus2.controller;

import kroryi.bus2.service.BusStopCsvImporterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import")
public class CsvImportController {

    private final BusStopCsvImporterService busStopCsvImporterService;

    @GetMapping("/bus-stop-details")
    public String importDetails() throws IOException {
        String csvPath = "C:/csv/BusStopInfo3.csv"; // ⬅ 너의 실제 경로로 바꿔줘
        busStopCsvImporterService.importFromCsv(csvPath);
        return "불러오기 완료!";
    }
}
