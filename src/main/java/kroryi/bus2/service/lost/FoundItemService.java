package kroryi.bus2.service.lost;

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

}
