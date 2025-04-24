package kroryi.bus2.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthenticationToken(String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        // 인증 여부는 이 시점에서 true로 설정하지 않고, 필터나 서비스에서 인증이 완료된 후에 설정하도록 변경
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    // 인증 상태 설정 메서드 추가
    public void setAuthenticatedStatus(boolean isAuthenticated) {
        setAuthenticated(isAuthenticated);
    }
}
