# TayoTayo 프로젝트 Docker Compose 설정

이 프로젝트는 TayoTayo 버스 시스템의 프론트엔드와 백엔드를 Docker Compose를 사용하여 구성한 것입니다.

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   External      │    │   Frontend      │    │   Backend       │
│   (15173)       │◄──►│   (5173)        │◄──►│   (8094)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                 │                       │
                    ┌─────────────────┐    ┌─────────────────┐
                    │   MySQL         │    │   Redis         │
                    │   (3306)        │    │   (6379)        │
                    └─────────────────┘    └─────────────────┘
```

## 🚀 빠른 시작

### 1. 사전 요구사항

- Docker
- Docker Compose
- OpenSSL (SSL 인증서 생성용)

### 2. 실행

```bash
# 실행 권한 부여
chmod +x start.sh

# 프로젝트 시작
./start.sh
```

또는 수동으로 실행:

```bash
# 필요한 디렉토리 생성
mkdir -p tayotayo/db/data
mkdir -p tayotayo/redis/data
mkdir -p nginx/ssl

# SSL 인증서 생성
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/key.pem \
    -out nginx/ssl/cert.pem \
    -subj "/C=KR/ST=Seoul/L=Seoul/O=TayoTayo/OU=IT/CN=docs.yi.or.kr"

# 컨테이너 빌드 및 시작
docker-compose up -d --build
```

## 📱 접속 정보

| 서비스 | URL | 포트 | 설명 |
|--------|-----|------|------|
| 프론트엔드 | https://docs.yi.or.kr:15173 | 15173 | Vue.js 애플리케이션 |
| 백엔드 API | https://docs.yi.or.kr:8094 | 8094 | Spring Boot API |
| 데이터베이스 | localhost:23306 | 23306 | MySQL 데이터베이스 |
| Redis | localhost:16379 | 16379 | Redis 캐시 |

## 🔧 서비스 구성

### Frontend (Vue.js)
- **포트**: 5173 (내부) / 15173 (외부)
- **기술**: Vue 3 + Vite + Tailwind CSS
- **프로토콜**: HTTPS (자체 서명 인증서)
- **역할**: 사용자 인터페이스 제공

### Backend (Spring Boot)
- **포트**: 8094
- **기술**: Spring Boot + Spring Security + JPA
- **역할**: API 서버 및 비즈니스 로직

### Database (MySQL)
- **포트**: 23306
- **데이터베이스**: bus
- **역할**: 데이터 저장소

### Redis
- **포트**: 16379
- **역할**: 세션 및 캐시 저장소

## 🔒 CORS 설정

프로젝트는 다음과 같이 CORS가 구성되어 있습니다:

### 백엔드 CORS 설정
- **허용된 오리진**: `https://docs.yi.or.kr:15173`
- **허용된 메서드**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **허용된 헤더**: 모든 헤더
- **Credentials**: true

### 프론트엔드 CORS 설정
- 직접 API 통신: `https://docs.yi.or.kr:8094`
- WebSocket 연결: `https://docs.yi.or.kr:8094/ws`
- 모든 API 요청은 `/api/` 경로로 라우팅
- 인증 요청은 `/auth/` 경로로 라우팅

## 📋 유용한 명령어

```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f [서비스명]
docker-compose logs -f frontend
docker-compose logs -f backend

# 컨테이너 재시작
docker-compose restart [서비스명]

# 컨테이너 중지
docker-compose down

# 이미지 재빌드
docker-compose build --no-cache

# 특정 서비스만 재시작
docker-compose restart frontend
docker-compose restart backend
```

## 🔧 환경 변수

### 백엔드 환경 변수
- `SPRING_PROFILES_ACTIVE`: prod
- `REDIRECT_BASE_URL`: https://docs.yi.or.kr:15173
- `SPRING_DATASOURCE_URL`: jdbc:mysql://database:3306/bus
- `SPRING_REDIS_HOST`: redis

### 프론트엔드 환경 변수
- `VITE_BASE_URL`: https://docs.yi.or.kr:8094

## 🛠️ 문제 해결

### 1. 포트 충돌
```bash
# 사용 중인 포트 확인
netstat -tulpn | grep :15173
netstat -tulpn | grep :8094

# 충돌하는 프로세스 종료
sudo kill -9 [PID]
```

### 2. SSL 인증서 문제
```bash
# 인증서 재생성
rm -rf nginx/ssl/*
./start.sh
```

### 3. 데이터베이스 연결 문제
```bash
# 데이터베이스 컨테이너 로그 확인
docker-compose logs database

# 데이터베이스 컨테이너 접속
docker-compose exec database mysql -u root -p
```

### 4. 빌드 실패
```bash
# 캐시 삭제 후 재빌드
docker-compose down
docker system prune -f
docker-compose build --no-cache
docker-compose up -d
```

## 📁 프로젝트 구조

```
tayotayo_pjt/
├── docker-compose.yml          # Docker Compose 설정
├── start.sh                    # 시작 스크립트
├── README.md                   # 이 파일
├── nginx/
│   ├── nginx.conf             # Nginx 설정
│   └── ssl/                   # SSL 인증서
├── frontendtayotayo/          # 프론트엔드 프로젝트
│   ├── Dockerfile
│   ├── nginx.conf
│   └── env.production
└── tayotayo/                  # 백엔드 프로젝트
    ├── Dockerfile
    ├── src/
    └── uploads/
```

## 🤝 기여하기

1. 이슈를 생성하거나 기존 이슈를 확인하세요
2. 새로운 기능을 위한 브랜치를 생성하세요
3. 변경사항을 커밋하고 푸시하세요
4. Pull Request를 생성하세요

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 