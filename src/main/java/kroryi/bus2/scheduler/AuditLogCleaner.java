package kroryi.bus2.scheduler;

import jakarta.transaction.Transactional;
import kroryi.bus2.repository.jpa.admin.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * 일주일에 한번 로그를 삭제하는 스케줄러
 */
public class AuditLogCleaner {

    private final AdminAuditLogRepository adminAuditLogRepository;

    // 매일 자정에 실행됨 임시로 09:15에 지워지게 설정
    @Transactional
    @Scheduled(cron = "0 15 9 * * *")
    public void deleteOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        int deleted = adminAuditLogRepository.deleteByTimestampBefore(cutoff);
        log.info("🧹 하루 지난 로그 {}건 삭제됨", deleted);
    }
}
