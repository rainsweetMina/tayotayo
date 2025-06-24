package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uri;
    
    private String errorMessage;
    
    private String stackTrace;
    
    private Integer statusCode;
    
    private String errorType;
    
    private LocalDateTime timestamp;
} 