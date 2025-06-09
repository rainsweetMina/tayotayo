package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notice_file")
public class NoticeFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;
    
    @Column(name = "stored_name", nullable = false)
    private String storedName;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_size")
    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;
}
