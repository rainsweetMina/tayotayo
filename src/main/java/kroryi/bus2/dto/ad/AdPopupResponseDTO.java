package kroryi.bus2.dto.ad;

import kroryi.bus2.entity.ad.Ad;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class AdPopupResponseDTO {

    private final Long id;
    private final String imageUrl;
    private final String linkUrl;
    private final String startDate;
    private final String endDate;

    public AdPopupResponseDTO(Ad ad) {
        this.id = ad.getId();
        this.imageUrl = ad.getImageUrl();
        this.linkUrl = ad.getLinkUrl();

        // LocalDateTime을 YYYY-MM-DD 형식의 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.startDate = ad.getStartDateTime() != null ? ad.getStartDateTime().toLocalDate().format(formatter) : null;
        this.endDate = ad.getEndDateTime() != null ? ad.getEndDateTime().toLocalDate().format(formatter) : null;
    }
}
