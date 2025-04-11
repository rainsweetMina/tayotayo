package kroryi.bus2.service;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.BusStopInfo;
import kroryi.bus2.repository.jpa.BusStopDetailRepository;
import kroryi.bus2.repository.jpa.BusStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BusStopCsvImporterService {

    private final BusStopDetailRepository busStopDetailRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public void importFromCsv(String csvFilePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
        String line;
        boolean isFirst = true;

        while ((line = reader.readLine()) != null) {
            if (isFirst) { isFirst = false; continue; } // skip header

            String[] tokens = line.split(",");

            // 정류장 존재 여부 확인
            String bsId = tokens[0].trim();
            if (!busStopRepository.existsByBsId(bsId)) {
                System.out.println("⚠️ 정류장 없음 - 건너뜀: " + bsId);
                continue; // 정류장 없으면 이 줄은 건너뜀
            }

            BusStopInfo detail = BusStopInfo.builder()
                    .bsId(tokens[0].trim())
                    .mId(tokens[1].trim())
                    .bsNmEn(tokens[3].trim())
                    .city(tokens[4].trim())
                    .district(tokens[5].trim())
                    .neighborhood(tokens[6].trim())
                    .routeCount(parseIntSafe(tokens[9]))
                    .build();

            busStopDetailRepository.save(detail);
        }

        reader.close();
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
