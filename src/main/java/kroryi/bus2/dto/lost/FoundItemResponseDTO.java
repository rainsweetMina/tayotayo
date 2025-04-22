package kroryi.bus2.dto.lost;

import kroryi.bus2.entity.lost.FoundItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FoundItemResponseDTO {

    private Long id;                   // 습득물 ID
    private String itemName;           // 지갑, 가방 등
    private String busCompany;         // 버스회사명
    private String busNumber;          // 노선번호
    private String foundPlace;         // 습득 위치
    private String content;            // 습득물 상세 설명
    private String handlerContact;     // 담당자 연락처
    private String handlerEmail;       // 담당자 이메일
    private String status;             // 처리상태 (문자열 변환)
    private String storageLocation;    // 보관장소
    private LocalDateTime foundTime;   // 습득 날짜
    private String photoUrl;           // 이미지 URL

    public static FoundItemResponseDTO fromEntity(FoundItem item) {
        return FoundItemResponseDTO.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .busCompany(item.getBusCompany())
                .busNumber(item.getBusNumber())
                .foundPlace(item.getFoundPlace())
                .content(item.getContent())
                .handlerContact(item.getHandlerContact())
                .handlerEmail(item.getHandlerEmail())
                .status(item.getStatus() != null ? item.getStatus().name() : null)
                .storageLocation(item.getStorageLocation())
                .foundTime(item.getFoundTime())
                .photoUrl(item.getPhoto() != null ? item.getPhoto().getUrl() : null)
                .build();
    }
}
