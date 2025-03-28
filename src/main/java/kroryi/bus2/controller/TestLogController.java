package kroryi.bus2.controller;

import kroryi.bus2.entity.ApiLog;
import kroryi.bus2.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestLogController {
    // 에러 제대로 받는지 확인용 컨트롤러. 추후 삭제 필요.
    private final ApiLogRepository apiLogRepository;

    @PostMapping("/error-log")
    public ResponseEntity<String> insertErrorLog() {
        ApiLog log = new ApiLog();
        log.setTimestamp(LocalDateTime.now());
        log.setSuccess(false);
        log.setErrorMessage("테스트 에러입니다!");

        apiLogRepository.save(log);

        return ResponseEntity.ok("에러 로그 1개 삽입 완료!");
    }
}
