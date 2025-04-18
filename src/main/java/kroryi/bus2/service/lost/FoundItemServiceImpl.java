package kroryi.bus2.service.lost;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import kroryi.bus2.entity.lost.*;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final LostItemRepository lostItemRepository;
    private final LostFoundMatchRepository matchRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    @Value("${file.url-prefix}")
    private String fileUrlPrefix;

    @Value("${file.upload.found-location}")
    private String fileUploadLocation;
    // ✅ 등록
    @AdminAudit(action = "습득물 등록", target = "FoundItem")
    @Override
    public void registerFoundItem(FoundItemRequestDTO dto, MultipartFile image) {
        User handler = userRepository.findById(dto.getHandlerId())
                .orElseThrow(() -> new EntityNotFoundException("담당자 없음"));

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
                .isDeleted(false)
                .isHidden(false)
                .visible(true)
                .matched(false)
                .build();

        foundItem.setStatus(FoundStatus.IN_STORAGE);
        foundItemRepository.save(foundItem); // 1차 저장

        if (image.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        Path uploadPath = Paths.get("uploads/found/");


        // ✅ 이미지 처리
        if (image != null && !image.isEmpty()) {
            try {

                Files.createDirectories(uploadPath);

                String originalFilename = image.getOriginalFilename();
                String ext = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                }

                String storedFileName = UUID.randomUUID() + "." + ext;
                Path targetPath = uploadPath.resolve(storedFileName);

                Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                Photo photo = Photo.builder()
                        .url(storedFileName)
                        .foundItem(foundItem)
                        .build();

                photoRepository.save(photo);

                // ✅ ✅ 이 부분이 핵심!!
                foundItem.setPhoto(photo);                  // 양방향 연결
                foundItemRepository.save(foundItem);        // 다시 저장

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }
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

        System.out.println("11111111111->" + item.getPhoto().getUrl());

        return FoundItemAdminResponseDTO.fromEntity(item);
    }

    // ✅ 수정
    @Override
    @AdminAudit(action = "습득물 수정", target = "FoundItem")
    public void updateFoundItem(Long id, FoundItemRequestDTO dto, MultipartFile image) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 습득물이 존재하지 않습니다."));

        // 1. 기본 정보 수정
        item.update(dto);

        // 2. 상태 업데이트
        if (dto.getStatus() == FoundStatus.RETURNED && !item.isMatched()) {
            item.setMatched(true);
        }

        // 3. 이미지 교체 처리
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            Photo oldPhoto = item.getPhoto();
            if (oldPhoto != null) {
                String oldPath = "uploads/found/" + oldPhoto.getUrl(); // ✅ 경로 포함
                try {
                    Files.deleteIfExists(Paths.get(oldPath));
                    item.setPhoto(null);
                    photoRepository.delete(oldPhoto);
                    photoRepository.flush();
                } catch (IOException e) {
                    throw new RuntimeException("기존 이미지 삭제 실패", e);
                }
            }

            // 새 이미지 저장
            try {
                String originalFilename = image.getOriginalFilename();
                String ext = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                }

                String storedFileName = UUID.randomUUID() + "." + ext;
                Path uploadPath = Paths.get("uploads/found/");
                Files.createDirectories(uploadPath);
                Path targetPath = uploadPath.resolve(storedFileName);

                Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                Photo newPhoto = Photo.builder()
                        .url(storedFileName)  // ✅ 파일명만 저장
                        .foundItem(item)
                        .build();

                photoRepository.save(newPhoto);
                item.setPhoto(newPhoto);

            } catch (IOException e) {
                throw new RuntimeException("새 이미지 저장 실패", e);
            }
        }

        foundItemRepository.save(item);
    }


    // ✅ 숨김 처리
    @Override
    @AdminTracked
    public void hideFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("숨길 습득물이 존재하지 않습니다."));
        item.setVisible(false);
        foundItemRepository.save(item);
    }

    // ✅ 삭제 처리
    @Override
    @AdminTracked
    public void deleteFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 습득물이 존재하지 않습니다."));
        item.setIsDeleted(true);
        foundItemRepository.save(item);
    }

    // ✅ 매칭 처리 - 로그인 사용자 기준
    @Override
    @Transactional
    @AdminAudit(action = "습득물 매칭", target = "FoundItem")
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


    //일반회원용 조회
    @Override
    public List<FoundItemResponseDTO> getVisibleFoundItemsForUser() {
        return foundItemRepository.findByIsDeletedFalseAndVisibleTrue().stream()
                .map(FoundItemResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public FoundItemResponseDTO getFoundItemDetailForUser(Long id) {
        FoundItem item = foundItemRepository.findByIdAndIsDeletedFalseAndVisibleTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 습득물이 존재하지 않거나 숨김/삭제되었습니다."));
        return FoundItemResponseDTO.fromEntity(item);
    }


}
