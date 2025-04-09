package kroryi.bus2.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final String email;
    private final String nickname;
    private final String userId;

    public CustomOAuth2User(OAuth2User oAuth2User, String email, String nickname, String userId) {
        this.oAuth2User = oAuth2User;
        this.email = email;
        this.nickname = nickname;
        this.userId = userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return userId; // OAuth2User가 유일하게 식별할 수 있도록 설정
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
}
