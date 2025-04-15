package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
