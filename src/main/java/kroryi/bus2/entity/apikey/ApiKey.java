package kroryi.bus2.entity.apikey;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import kroryi.bus2.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(unique = true, name = "api_key")
    private String apikey;

    private String name;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    private String allowedIp;

    private LocalDateTime createdAt;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApiKeyStatus status = ApiKeyStatus.PENDING;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "apiKey", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ApiKeyCallbackUrl> callbackUrls = new ArrayList<>();
}
