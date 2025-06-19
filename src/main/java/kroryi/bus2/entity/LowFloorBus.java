package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "low_floor_bus")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LowFloorBus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    private String author;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    
    // 조회수 필드
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    // 커스텀 생성자
    public LowFloorBus(String title, String author, String content) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.viewCount = 0L;
    }

    // 파일 업로드
    @OneToMany(mappedBy = "lowFloorBus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LowFloorBusFile> files = new ArrayList<>();

    // 안전한 방식으로 파일들 교체하는 메서드
    public void updateFiles(List<LowFloorBusFile> newFiles) {
        this.files.clear(); // 기존 파일들 orphan 처리
        for (LowFloorBusFile file : newFiles) {
            this.addFile(file);
        }
    }

    // 개별 파일 추가 시 연관관계까지 묶는 메서드
    public void addFile(LowFloorBusFile file) {
        this.files.add(file);
        file.setLowFloorBus(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Timestamp(System.currentTimeMillis());
    }
} 