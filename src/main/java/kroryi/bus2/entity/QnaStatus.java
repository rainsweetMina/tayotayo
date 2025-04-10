package kroryi.bus2.entity;

public enum QnaStatus {
    WAITING("답변 대기"),
    ANSWERED("답변 완료"),
    HIDDEN("숨김 처리");

    private final String description;

    QnaStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

