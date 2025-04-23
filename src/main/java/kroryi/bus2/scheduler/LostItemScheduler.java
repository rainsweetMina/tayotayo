package kroryi.bus2.scheduler;

import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.repository.jpa.LostItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LostItemScheduler {

    private final LostItemRepository lostItemRepository;

    //    @Scheduled(fixedDelay = 10000) // 10초마다 실행 (테스트용)
    // 매일 새벽 1시 1분 실행
    @Scheduled(cron = "0 1 1 * * *")
    public void hideOldLostItems() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        // createdAt 기준으로 7일 경과된 게시글 조회
        List<LostItem> oldItems = lostItemRepository.findByVisibleTrueAndCreatedAtBefore(cutoff);

        if (oldItems.isEmpty()) {
            log.info("숨길 분실물 없음");
            return;
        }

        oldItems.forEach(item -> item.setVisible(false));
        lostItemRepository.saveAll(oldItems);

        log.info("{}개의 분실물을 숨김 처리했습니다.", oldItems.size());
    }
}
