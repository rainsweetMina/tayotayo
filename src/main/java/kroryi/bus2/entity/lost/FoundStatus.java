package kroryi.bus2.entity.lost;

import lombok.Getter;

@Getter
public enum FoundStatus {
    IN_STORAGE("보관중"),
    RETURNED("수령완료");

    private final String displayName;

    FoundStatus(String displayName) {
        this.displayName = displayName;
    }

}

