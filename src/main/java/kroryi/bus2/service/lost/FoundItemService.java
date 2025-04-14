package kroryi.bus2.service.lost;

import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FoundItemService {

    // 등록
    void registerFoundItem(FoundItemRequestDTO dto);

    // 전체 목록 조회 (삭제되지 않은 것만)
    List<FoundItemAdminResponseDTO> getAllFoundItems();

    // 상세 조회
    FoundItemAdminResponseDTO getFoundItemById(Long id);

    // 수정
    void updateFoundItem(Long id, FoundItemRequestDTO dto);

    // 숨김 처리
    void hideFoundItem(Long id);

    // 삭제 처리
    void deleteFoundItem(Long id);


    // ✅ 매칭 처리
    @Transactional
    void matchFoundItem(Long foundItemId, Long lostItemId);

    // 매칭 처리 - 핸들러 ID 직접 지정 (관리자 페이지 등에서 사용)
    void matchFoundItem(Long foundItemId, Long lostItemId, Long handlerId);

    List<FoundItemAdminResponseDTO> getAllForAdmin();

    FoundItemAdminResponseDTO getFoundItemAdminById(Long id);

}

