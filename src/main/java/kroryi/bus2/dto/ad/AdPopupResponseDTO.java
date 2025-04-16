package kroryi.bus2.dto.ad;

import kroryi.bus2.entity.ad.Ad;
import lombok.Getter;

@Getter
public class AdPopupResponseDTO {

    private final String imageUrl;
    private final String linkUrl;

    public AdPopupResponseDTO(Ad ad) {
        this.imageUrl = ad.getImageUrl();
        this.linkUrl = ad.getLinkUrl();
    }
}
