package kroryi.bus2.service.lost;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemListResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO; // lost 폴더 기준
import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import kroryi.bus2.entity.lost.FoundItem;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;

    public FoundItem saveFoundItem(FoundItemRequestDTO dto) {
        User handler = userRepository.findById(dto.getHandlerId())
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        FoundItem item = FoundItem.builder()
                .itemName(dto.getItemName())
                .busCompany(dto.getBusCompany())
                .busNumber(dto.getBusNumber())
                .foundPlace(dto.getFoundPlace())
                .content(dto.getContent())
                .handlerContact(dto.getHandlerContact())
                .handlerEmail(dto.getHandlerEmail())
                .status(dto.getStatus())
                .storageLocation(dto.getStorageLocation())
                .foundTime(dto.getFoundTime())
                .photoUrl(dto.getPhotoUrl())
                .handler(handler)
                .matched(false)
                .visible(true)
                .build();

        return foundItemRepository.save(item);
    }

    //습득물 전체 조회
    public List<FoundItemListResponseDTO> getAllFoundItems() {
        return foundItemRepository.findAll().stream()
                .map(item -> new FoundItemListResponseDTO(
                        item.getId(),
                        item.getItemName(),
                        item.getBusCompany(),
                        item.getFoundTime(),
                        item.isMatched()
                ))
                .collect(Collectors.toList());
    }

    //습득물 개별 조회
    public FoundItemResponseDTO getFoundItem(Long id) {
        FoundItem foundItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 습득물이 없습니다."));

        String photoUrl = (foundItem.getPhoto() != null)
                ? foundItem.getPhoto().getUrl()
                : null;

        return FoundItemResponseDTO.builder()
                .id(foundItem.getId())
                .itemName(foundItem.getItemName())
                .busCompany(foundItem.getBusCompany())
                .busNumber(foundItem.getBusNumber())
                .foundPlace(foundItem.getFoundPlace())
                .content(foundItem.getContent())
                .handlerContact(foundItem.getHandlerContact())
                .handlerEmail(foundItem.getHandlerEmail())
                .status(foundItem.getStatus())
                .storageLocation(foundItem.getStorageLocation())
                .foundTime(foundItem.getFoundTime())
                .photoUrl(photoUrl)
                .build();
    }
    public List<FoundItemAdminResponseDTO> getAllForAdmin() {
        return foundItemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(found -> FoundItemAdminResponseDTO.builder()
                        .id(found.getId())
                        .itemName(found.getItemName())
                        .busNumber(found.getBusNumber())
                        .foundPlace(found.getFoundPlace())
                        .handlerId(found.getHandler().getId())
                        .status(found.getStatus())
                        .visible(found.isVisible())
                        .deleted(found.isDeleted())
                        .createdAt(found.getCreatedAt())
                        .updatedAt(found.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    public FoundItemAdminResponseDTO getFoundItemAdminById(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 습득물이 존재하지 않습니다."));

        return FoundItemAdminResponseDTO.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .busNumber(item.getBusNumber())
                .foundPlace(item.getFoundPlace())
                .handlerId(item.getHandler().getId())
                .status(item.getStatus())
                .visible(item.isVisible())
                .deleted(item.isDeleted())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    public void registerFoundItem(FoundItemRequestDTO dto) {
        User handler = userRepository.findById(dto.getHandlerId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않음"));

        FoundItem item = FoundItem.builder()
                .itemName(dto.getItemName())
                .busCompany(dto.getBusCompany())
                .busNumber(dto.getBusNumber())
                .foundPlace(dto.getFoundPlace())
                .content(dto.getContent())
                .handlerContact(dto.getHandlerContact())
                .handlerEmail(dto.getHandlerEmail())
                .status(dto.getStatus())
                .storageLocation(dto.getStorageLocation())
                .foundTime(dto.getFoundTime())
                .photoUrl(dto.getPhotoUrl())
                .handler(handler)
                .visible(true) // 기본값
                .deleted(false) // 기본값
                .build();

        foundItemRepository.save(item);
    }
    public void hideFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("습득물이 존재하지 않습니다."));

        item.setVisible(false);
    }
    public void deleteFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("습득물이 존재하지 않습니다."));

        item.setDeleted(true);
    }




}
