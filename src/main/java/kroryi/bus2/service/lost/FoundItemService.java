package kroryi.bus2.service.lost;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemListResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoundItemService {

//    void registerFoundItem(FoundItemRequestDTO dto); // 기존 등록

    // 이미지 포함 버전 (어노테이션 제거)
    void registerFoundItem(FoundItemRequestDTO dto, MultipartFile image);


    // 전체 목록 조회 (삭제되지 않은 것만)
    List<FoundItemAdminResponseDTO> getAllFoundItems();

    // 상세 조회
    FoundItemAdminResponseDTO getFoundItemById(Long id);

    // 수정
    void updateFoundItem(Long id, FoundItemRequestDTO dto, MultipartFile image);

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

    List<FoundItemResponseDTO> getVisibleFoundItemsForUser();
    FoundItemResponseDTO getFoundItemDetailForUser(Long id);

    List<FoundItemListResponseDTO> getFoundItemsForPublic();


}

