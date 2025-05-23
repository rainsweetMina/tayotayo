package kroryi.bus2.entity.user;

import jakarta.persistence.*;
import kroryi.bus2.converter.RoleConverter;
import kroryi.bus2.entity.lost.FoundItem;
import kroryi.bus2.entity.lost.LostFoundMatch;
import kroryi.bus2.entity.lost.LostItem;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")  // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 고유 아이디 (PK)

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;  // 유저 아이디

    @Column(nullable = false)
    private String username;  // 이름

    @Column(nullable = false)
    private String password;  // 비밀번호

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Convert(converter = RoleConverter.class)
    @Column(nullable = false)
    private Role role;  // 권한 (예: USER, ADMIN 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_type", nullable = false)
    private SignupType signupType = SignupType.GENERAL; // 가입유형 (ex. 일반, 소셜 등)

    @Column(name = "signup_date")
    private LocalDate signupDate;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "handler")
    private Set<FoundItem> foundItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "matchedBy")
    private Set<LostFoundMatch> lostFoundMatches = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reporter")
    private Set<LostItem> lostItems = new LinkedHashSet<>();

    @Column(name = "withdraw", nullable = false)
    private boolean withdraw = false;  // 기본값은 false

}
