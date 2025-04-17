package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String author;
    private Timestamp createdDate;
    private Timestamp updatedDate;


    // ✅ 커스텀 생성자 추가
    public Notice(String title, String author, String content) {
        this.title = title;
        this.author = author;
        this.content = content;
    }

    // 파일 업로드
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeFile> files = new ArrayList<>();

    // ✅ 안전한 방식으로 파일들 교체하는 메서드 추가
    public void updateFiles(List<NoticeFile> newFiles) {
        this.files.clear(); // 기존 파일들 orphan 처리
        for (NoticeFile file : newFiles) {
            this.addFile(file);
        }
    }

    // ✅ 개별 파일 추가 시 연관관계까지 묶는 메서드
    public void addFile(NoticeFile file) {
        this.files.add(file);
        file.setNotice(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Timestamp(System.currentTimeMillis());
    }

    //팝업관련
    @Column(name = "show_popup", nullable = false)
    private boolean showPopup = false;

    @Column(name = "popup_start")
    private LocalDateTime popupStart;

    @Column(name = "popup_end")
    private LocalDateTime popupEnd;


}
