# JWT 토큰 인증 가이드

## 개요
이 프로젝트는 JWT(JSON Web Token) 기반 인증 시스템을 구현하여 사용자 인증을 처리합니다.

## 주요 기능

### 1. JWT 토큰 생성
- **Access Token**: 1시간 유효 (기본값)
- **Refresh Token**: 7일 유효 (기본값)

### 2. 인증 엔드포인트

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
    "userId": "사용자ID",
    "password": "비밀번호"
}
```

**응답:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
    "refreshToken": "리프레시토큰"
}
```

#### 토큰 검증
```http
GET /api/auth/validate
Authorization: Bearer {accessToken}
```

#### 로그아웃
```http
POST /api/auth/logout
```

### 3. 보호된 API 사용

JWT 토큰을 사용하여 보호된 API에 접근할 때는 Authorization 헤더에 Bearer 토큰을 포함해야 합니다:

```http
GET /api/user/info
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 설정

### application.properties
```properties
# JWT 설정
kroryi.jwt.secret=your-secret-key-here
kroryi.jwt.access-token-expiration=3600000
kroryi.jwt.refresh-token-expiration=604800000
```

## 구현된 컴포넌트

### 1. JwtTokenUtil
- JWT 토큰 생성 및 검증
- Access Token과 Refresh Token 생성
- 토큰 만료 확인

### 2. JwtAuthenticationFilter
- 모든 요청에서 JWT 토큰 검증
- Authorization 헤더에서 Bearer 토큰 추출
- 토큰 유효성 검사 및 사용자 인증

### 3. JwtAuthController
- 로그인, 토큰 갱신, 로그아웃 API 제공
- 토큰 검증 엔드포인트

### 4. Spring Security 설정
- JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
- JWT 인증 엔드포인트 허용

## OAuth2 로그인과의 통합

OAuth2 로그인 성공 시에도 JWT 토큰이 자동으로 생성되어 URL 파라미터로 전달됩니다:

```
https://localhost:5173/mypage?accessToken=...&refreshToken=...
```

## 보안 고려사항

1. **토큰 만료**: Access Token은 짧은 시간(1시간)으로 설정
2. **Refresh Token**: Access Token 갱신용으로만 사용
3. **HTTPS**: 프로덕션 환경에서는 반드시 HTTPS 사용
4. **토큰 저장**: 클라이언트에서는 안전한 방법으로 토큰 저장 (HttpOnly 쿠키 권장)

## 클라이언트 사용 예시

### JavaScript (Fetch API)
```javascript
// 로그인
const loginResponse = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        userId: 'user123',
        password: 'password123'
    })
});

const { accessToken, refreshToken } = await loginResponse.json();

// 보호된 API 호출
const userInfoResponse = await fetch('/api/user/info', {
    headers: {
        'Authorization': `Bearer ${accessToken}`
    }
});
```

### 토큰 갱신
```javascript
// Access Token 만료 시 Refresh Token으로 갱신
const refreshResponse = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        refreshToken: refreshToken
    })
});

const { accessToken: newAccessToken } = await refreshResponse.json();
```

## 에러 처리

JWT 토큰 관련 에러는 다음과 같이 처리됩니다:

- **토큰 만료**: 401 Unauthorized
- **잘못된 토큰**: 401 Unauthorized
- **토큰 형식 오류**: 400 Bad Request

## 로깅

JWT 인증 관련 로그는 다음과 같이 출력됩니다:

- `DEBUG`: JWT 인증 성공
- `WARN`: 잘못된 토큰 타입, 사용자 없음
- `ERROR`: JWT 토큰 처리 중 오류 