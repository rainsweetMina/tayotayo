package kroryi.bus2.service.lost;


import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.lost.*;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {
    private final LostItemRepository lostItemRepository;
    private final UserRepository userRepository;
    private final FoundItemRepository foundItemRepository;
    private final LostFoundMatchRepository lostFoundMatchRepository;

    @AdminAudit(action = "분실물 등록", target = "LostItem")
    public LostItem saveLostItem(LostItemRequestDTO dto) {
        // 신고자 유저 불러오기
        User reporter = userRepository.findById(dto.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("신고자 정보가 없습니다."));

        LostItem item = LostItem.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .busNumber(dto.getBusNumber())
                .busCompany(dto.getBusCompany())
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
                .filter(item -> item.isVisible() && !item.isDeleted()) // ✅ 조건 추가
                .map(item -> LostItemListResponseDTO.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .content(item.getContent())
                        .busNumber(item.getBusNumber())
                        .busCompany(item.getBusCompany())
                        .lostTime(item.getLostTime())
                        .build())
                .toList();
    }
    public LostItem findById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 분실물이 존재하지 않습니다."));
    }

    public List<LostItemAdminResponseDTO> getAllForAdmin() {
        return lostItemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(lost -> {
                    System.out.println("버스번호 확인: " + lost.getBusNumber()); // ✅ 이거 찍어보자
                    return LostItemAdminResponseDTO.builder()
                            .id(lost.getId())
                            .title(lost.getTitle())
                            .busNumber(lost.getBusNumber())
                            .busCompany(lost.getBusCompany())
                            .memberId(lost.getReporter().getId())
                            .deleted(lost.isDeleted())
                            .visible(lost.isVisible())
                            .createdAt(lost.getCreatedAt())
                            .updatedAt(lost.getUpdatedAt())
                            .build();
                })
                .toList();
    }
    private LostItemResponseDTO toResponseDTO(LostItem lostItem) {
        return LostItemResponseDTO.builder()
                .id(lostItem.getId())
                .title(lostItem.getTitle())
                .content(lostItem.getContent())
                .busNumber(lostItem.getBusNumber())
                .lostTime(lostItem.getLostTime())
                .memberId(lostItem.getReporter().getId()) // User 엔티티 대신 id만
                .matched(lostItem.isMatched())
                .visible(lostItem.isVisible())
                .deleted(lostItem.isDeleted())
                .createdAt(lostItem.getCreatedAt())
                .updatedAt(lostItem.getUpdatedAt())
                .build();
    }
    @Transactional
    @AdminAudit(action = "분실물 숨김", target = "LostItem")
    public void hideLostItem(Long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 분실물을 찾을 수 없습니다."));

        item.setVisible(false); // ✅ 숨김 처리
    }
    @Transactional
    @AdminAudit(action = "분실물 삭제", target = "LostItem")
    public void deleteLostItem(Long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 분실물을 찾을 수 없습니다."));

        item.setDeleted(true);  // ✅ 실제 삭제는 하지 않고, soft delete
    }
    public LostItemResponseDTO getLostItemById(Long id) {
        LostItem item = findById(id);

        // 숨김/삭제된 게시글은 일반회원은 못 봄
        if (!item.isVisible() || item.isDeleted()) {
            throw new RuntimeException("조회할 수 없는 게시글입니다.");
        }

        return LostItemResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .busNumber(item.getBusNumber())
                .lostTime(item.getLostTime())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    public LostItemAdminResponseDTO getLostItemAdminById(Long id) {
        LostItem item = findById(id);

        return LostItemAdminResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .busNumber(item.getBusNumber())
                .busCompany(item.getBusCompany())
                .lostTime(item.getLostTime())
                .visible(item.isVisible())
                .deleted(item.isDeleted())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .memberId(item.getReporter().getId())
                .build();
    }

    public LostItemStatsDTO getLostItemStatistics() {
        long lostItemCount = lostItemRepository.count();  // 전체 분실물 수
        long foundItemCount = foundItemRepository.count();  // 전체 습득물 수
        long matchedCount = lostFoundMatchRepository.countByMatchedAtIsNotNull();  // 매칭된 건 수
        long unmatchedCount = lostItemCount - matchedCount;  // 매칭되지 않은 건수

        return LostItemStatsDTO.builder()
                .lostItemCount(lostItemCount)
                .foundItemCount(foundItemCount)
                .matchedCount(matchedCount)
                .unmatchedCount(unmatchedCount)
                .build();
    }








}
