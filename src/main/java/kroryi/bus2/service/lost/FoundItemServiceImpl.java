package kroryi.bus2.service.lost;

import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.lost.FoundItem;
import kroryi.bus2.entity.lost.FoundStatus;
import kroryi.bus2.entity.lost.LostFoundMatch;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final LostItemRepository lostItemRepository;
    private final LostFoundMatchRepository matchRepository;
    private final UserRepository userRepository;

    // ✅ 등록
    @Override
    public void registerFoundItem(FoundItemRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User handler = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보로 사용자를 찾을 수 없습니다."));

        if (!handler.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("버스회사 관리자만 습득물을 등록할 수 있습니다.");
        }

        FoundItem foundItem = FoundItem.builder()
                .itemName(dto.getItemName())
                .busCompany(dto.getBusCompany())
                .busNumber(dto.getBusNumber())
                .foundPlace(dto.getFoundPlace())
                .content(dto.getContent())
                .handler(handler)
                .handlerContact(dto.getHandlerContact())
                .handlerEmail(dto.getHandlerEmail())
                .status(dto.getStatus())
                .storageLocation(dto.getStorageLocation())
                .foundTime(dto.getFoundTime().atStartOfDay())
                .photoUrl(dto.getPhotoUrl())
                .isDeleted(false)
                .isHidden(false)
                .visible(true)
                .matched(false)
                .build();

        foundItem.setStatus(FoundStatus.IN_STORAGE); // 자동으로 '보관중' 상태로 설정
        foundItemRepository.save(foundItem);
    }

    // ✅ 목록 조회
    @Override
    public List<FoundItemAdminResponseDTO> getAllFoundItems() {
        return foundItemRepository.findByIsDeletedFalse().stream()
                .map(FoundItemAdminResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 상세 조회
    @Override
    public FoundItemAdminResponseDTO getFoundItemById(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 습득물을 찾을 수 없습니다."));
        return FoundItemAdminResponseDTO.fromEntity(item);
    }

    // ✅ 수정
    @Override
    public void updateFoundItem(Long id, FoundItemRequestDTO dto) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 습득물이 존재하지 않습니다."));
        item.update(dto);
        foundItemRepository.save(item);
    }

    // ✅ 숨김 처리
    @Override
    public void hideFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("숨길 습득물이 존재하지 않습니다."));
        item.setVisible(false);
        foundItemRepository.save(item);
    }

    // ✅ 삭제 처리
    @Override
    public void deleteFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 습득물이 존재하지 않습니다."));
        item.setIsDeleted(true);
        foundItemRepository.save(item);
    }

    // ✅ 매칭 처리 - 로그인 사용자 기준
    @Override
    @Transactional
    public void matchFoundItem(Long foundItemId, Long lostItemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User handler = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
        matchFoundItem(foundItemId, lostItemId, handler.getId());
    }

    // ✅ 매칭 처리 - 관리자 ID 직접 지정
    @Override
    @Transactional
    public void matchFoundItem(Long foundItemId, Long lostItemId, Long handlerId) {
        boolean alreadyMatched = matchRepository.existsByFoundItemIdAndLostItemId(foundItemId, lostItemId);
        if (alreadyMatched) {
            throw new IllegalArgumentException("이미 매칭된 항목입니다.");
        }

        boolean lostMatched = matchRepository.existsByLostItemId(lostItemId);
        if (lostMatched) {
            throw new IllegalStateException("이미 다른 습득물과 매칭된 분실물입니다.");
        }

        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 습득물 ID입니다."));
        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 분실물 ID입니다."));
        User handler = userRepository.findById(handlerId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        LostFoundMatch match = new LostFoundMatch();
        match.setFoundItem(foundItem);
        match.setLostItem(lostItem);
        match.setMatchedBy(handler);
        match.setMatchedAt(LocalDateTime.now());

        matchRepository.save(match);
        foundItem.matchAndComplete();
        foundItemRepository.save(foundItem);
    }

    // ✅ 관리자용 전체 조회
    @Override
    public List<FoundItemAdminResponseDTO> getAllForAdmin() {
        return foundItemRepository.findByIsDeletedFalse().stream()
                .map(FoundItemAdminResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 관리자용 상세 조회
    @Override
    public FoundItemAdminResponseDTO getFoundItemAdminById(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 습득물을 찾을 수 없습니다."));
        return FoundItemAdminResponseDTO.fromEntity(item);
    }
    // ✅ 관리자용 매칭 통계(대시보드에 나중에 띄울용도)
    public Long getMatchedCount() {
        return foundItemRepository.countMatchedIncludingManual();
    }


}
