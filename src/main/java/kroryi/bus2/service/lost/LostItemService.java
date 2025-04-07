package kroryi.bus2.service.lost;


import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {
    private final LostItemRepository lostItemRepository;
    private final UserRepository userRepository;

    public LostItem saveLostItem(LostItemRequestDTO dto) {
        // 신고자 유저 불러오기
        User reporter = userRepository.findById(dto.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("신고자 정보가 없습니다."));

        LostItem item = LostItem.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .busNumber(dto.getBusNumber())
                .lostTime(dto.getLostTime() != null ? dto.getLostTime() : LocalDateTime.now())
                .reporter(reporter)
                .matched(false)
                .visible(true) // ✅ 명시적 설정
                .build();

        return lostItemRepository.save(item);
    }

    // ✅ 관리자용: 전체 조회 (숨김 포함)
    public List<LostItem> getAllLostItemsIncludingHidden() {
        return lostItemRepository.findAllIncludingHidden();
    }

    // 🔹 기존: 일반 회원용
    public List<LostItem> getAllLostItemsVisibleOnly() {
        return lostItemRepository.findAllByVisibleTrue();
    }

    public List<LostItemListResponseDTO> getAllLostItems() {
        return lostItemRepository.findAll().stream()
                .map(item -> LostItemListResponseDTO.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .busNumber(item.getBusNumber())
                        .lostTime(item.getLostTime())
                        .matched(item.isMatched())
                        .build())
                .toList();
    }
    public LostItem findById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 분실물이 존재하지 않습니다."));
    }

}
