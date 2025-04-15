package kroryi.bus2.service.lost;

import kroryi.bus2.dto.lost.LostFoundMatchRequestDTO;
import kroryi.bus2.entity.lost.FoundItem;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.entity.lost.LostFoundMatch;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LostFoundMatchService {

    private final LostFoundMatchRepository matchRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;

    public LostFoundMatch matchItems(LostFoundMatchRequestDTO dto) {
        LostItem lost = lostItemRepository.findById(dto.getLostItemId())
                .orElseThrow(() -> new IllegalArgumentException("분실물 정보가 없습니다."));

        FoundItem found = foundItemRepository.findById(dto.getFoundItemId())
                .orElseThrow(() -> new IllegalArgumentException("습득물 정보가 없습니다."));

        User matchedBy = userRepository.findById(dto.getMatchedById())
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        // 매칭 상태 true로 업데이트
        lost.setMatched(true);
        found.setMatched(true);

        LostFoundMatch match = LostFoundMatch.builder()
                .lostItem(lost)
                .foundItem(found)
                .matchedAt(LocalDateTime.now())
                .matchedBy(matchedBy)
                .build();

        return matchRepository.save(match);
    }
}
