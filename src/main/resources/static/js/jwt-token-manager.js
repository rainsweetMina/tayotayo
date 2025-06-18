/**
 * JWT 토큰 관리 유틸리티
 * 로컬 스토리지, 세션 스토리지, 쿠키를 통해 토큰을 관리합니다.
 */
class JwtTokenManager {
    constructor() {
        this.ACCESS_TOKEN_KEY = 'accessToken';
        this.REFRESH_TOKEN_KEY = 'refreshToken';
        this.TOKEN_EXPIRY_KEY = 'tokenExpiry';
    }

    /**
     * URL 파라미터에서 토큰 추출
     */
    extractTokensFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        const accessToken = urlParams.get('accessToken');
        const refreshToken = urlParams.get('refreshToken');
        
        if (accessToken) {
            this.setAccessToken(accessToken);
        }
        if (refreshToken) {
            this.setRefreshToken(refreshToken);
        }
        
        // URL에서 토큰 파라미터 제거
        if (accessToken || refreshToken) {
            const newUrl = new URL(window.location);
            newUrl.searchParams.delete('accessToken');
            newUrl.searchParams.delete('refreshToken');
            window.history.replaceState({}, document.title, newUrl.pathname + newUrl.search);
        }
        
        return { accessToken, refreshToken };
    }

    /**
     * Access Token 설정
     */
    setAccessToken(token) {
        localStorage.setItem(this.ACCESS_TOKEN_KEY, token);
        // 토큰 만료 시간 설정 (1시간 후)
        const expiry = new Date().getTime() + (60 * 60 * 1000);
        localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiry.toString());
    }

    /**
     * Refresh Token 설정
     */
    setRefreshToken(token) {
        localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
    }

    /**
     * Access Token 가져오기
     */
    getAccessToken() {
        return localStorage.getItem(this.ACCESS_TOKEN_KEY);
    }

    /**
     * Refresh Token 가져오기
     */
    getRefreshToken() {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    }

    /**
     * 토큰 만료 확인
     */
    isTokenExpired() {
        const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
        if (!expiry) return true;
        
        return new Date().getTime() > parseInt(expiry);
    }

    /**
     * 토큰 유효성 확인
     */
    hasValidToken() {
        const token = this.getAccessToken();
        return token && !this.isTokenExpired();
    }

    /**
     * 토큰 갱신
     */
    async refreshToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            throw new Error('Refresh token이 없습니다.');
        }

        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken })
            });

            if (response.ok) {
                const data = await response.json();
                this.setAccessToken(data.accessToken);
                return data.accessToken;
            } else {
                throw new Error('토큰 갱신에 실패했습니다.');
            }
        } catch (error) {
            console.error('토큰 갱신 오류:', error);
            this.clearTokens();
            throw error;
        }
    }

    /**
     * 토큰 검증
     */
    async validateToken() {
        const token = this.getAccessToken();
        if (!token) {
            return false;
        }

        try {
            const response = await fetch('/api/auth/validate', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            return response.ok;
        } catch (error) {
            console.error('토큰 검증 오류:', error);
            return false;
        }
    }

    /**
     * 모든 토큰 삭제
     */
    clearTokens() {
        localStorage.removeItem(this.ACCESS_TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
    }

    /**
     * 로그아웃
     */
    async logout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.getAccessToken()}`
                }
            });
        } catch (error) {
            console.error('로그아웃 오류:', error);
        } finally {
            this.clearTokens();
            // 로그인 페이지로 리다이렉트
            window.location.href = '/auth/login';
        }
    }

    /**
     * 인증 헤더 생성
     */
    getAuthHeaders() {
        const token = this.getAccessToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    /**
     * 자동 토큰 갱신이 포함된 fetch 래퍼
     */
    async authenticatedFetch(url, options = {}) {
        // 토큰이 만료되었으면 갱신 시도
        if (this.isTokenExpired() && this.getRefreshToken()) {
            try {
                await this.refreshToken();
            } catch (error) {
                // 갱신 실패 시 로그인 페이지로 리다이렉트
                console.error('토큰 갱신 실패:', error);
                this.clearTokens();
                window.location.href = '/auth/login';
                return;
            }
        }

        // 인증 헤더 추가
        const headers = {
            ...this.getAuthHeaders(),
            ...options.headers
        };

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            // 401 에러 시 토큰 갱신 시도
            if (response.status === 401 && this.getRefreshToken()) {
                try {
                    await this.refreshToken();
                    // 갱신 후 재시도
                    const retryResponse = await fetch(url, {
                        ...options,
                        headers: {
                            ...this.getAuthHeaders(),
                            ...options.headers
                        }
                    });
                    return retryResponse;
                } catch (error) {
                    console.error('토큰 갱신 후 재시도 실패:', error);
                    this.clearTokens();
                    window.location.href = '/auth/login';
                    return;
                }
            }

            // 400 에러 처리 - 상세한 에러 정보 로깅
            if (response.status === 400) {
                try {
                    const errorData = await response.json();
                    console.error('400 에러 상세 정보:', {
                        url: url,
                        method: options.method || 'GET',
                        error: errorData,
                        requestHeaders: headers
                    });
                } catch (parseError) {
                    console.error('400 에러 응답 파싱 실패:', parseError);
                }
            }

            return response;
        } catch (error) {
            console.error('API 요청 오류:', error);
            throw error;
        }
    }
}

// 전역 인스턴스 생성
const tokenManager = new JwtTokenManager();

// 페이지 로드 시 URL에서 토큰 추출
document.addEventListener('DOMContentLoaded', function() {
    tokenManager.extractTokensFromUrl();
});

// 전역으로 사용할 수 있도록 window 객체에 추가
window.tokenManager = tokenManager; 