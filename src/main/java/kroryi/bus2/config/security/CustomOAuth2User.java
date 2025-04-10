package kroryi.bus2.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final String userId;
    private final String email;
    private final String nickname;
    private final String role;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(String userId, String email, String nickname, String role, Map<String, Object> attributes) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인 사용자에게 비밀번호는 필요 없으므로 null 반환
    }

    @Override
    public String getUsername() {
        return userId; // userId를 username으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 소셜 로그인 사용자는 계정 만료 여부를 다루지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 소셜 로그인 사용자이므로 활성화된 상태로 처리
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // 소셜 로그인 사용자 속성
    }

    @Override
    public String getName() {
        return userId; // AuthenticatedPrincipal에서 요구하는 getName() 메서드 추가
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
