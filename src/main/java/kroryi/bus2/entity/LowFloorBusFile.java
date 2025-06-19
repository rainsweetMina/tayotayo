package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "low_floor_bus_file")
@NoArgsConstructor
@Getter
@Setter
public class LowFloorBusFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "low_floor_bus_id")
    private LowFloorBus lowFloorBus;

    private String originalName;
    private String storedName;
    private String fileType;
    private Long fileSize;

    public LowFloorBusFile(String originalName, String storedName, String fileType, Long fileSize) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
} 