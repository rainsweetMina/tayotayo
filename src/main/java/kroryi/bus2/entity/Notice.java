package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "notice")
@Data
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String author;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Timestamp(System.currentTimeMillis());
    }

}
