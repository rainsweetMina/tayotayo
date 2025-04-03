package kroryi.bus2.scheduler;

import groovy.util.logging.Slf4j;
import kroryi.bus2.entity.LostItem;
import kroryi.bus2.repository.jpa.LostItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Log4j2
public class LostItemScheduler {

    private final LostItemRepository lostItemRepository;

    @Scheduled(cron = "0 1 1 * * *") // 매일 새벽 1시
    public void hideOldLostItems() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<LostItem> oldItems = lostItemRepository.findByVisibleTrueAndLostTimeBefore(cutoff);

        if (oldItems.isEmpty()) {
            log.info("숨길 분실물 없음");
            return;
        }

        oldItems.forEach(item -> item.setVisible(false));
        lostItemRepository.saveAll(oldItems);

        log.info("{}개의 분실물을 숨김 처리했습니다.", oldItems.size());
    }
}

