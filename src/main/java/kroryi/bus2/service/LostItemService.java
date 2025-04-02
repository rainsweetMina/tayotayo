package kroryi.bus2.service;


import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.entity.LostItem;
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
                .build();

        return lostItemRepository.save(item);
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
}
