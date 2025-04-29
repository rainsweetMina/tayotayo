# 프로젝트 이름: 대구광역시 버스 API 활용

## 개요
저희 프로젝트는 버스 및 노선 관련 데이터를 관리하고 클라이언트(웹/앱)에 실시간 정보를 제공하는 REST API 기반의 프로젝트입니다. Spring Boot를 기반으로 개발되었으며, Redis와 JPA를 활용하여 데이터를 효율적으로 처리합니다.

---

## 주요 기능
- **노선 관리**: 노선 추가, 수정, 삭제 및 경유 정류소 관리
- **실시간 데이터 제공**: WebSocket을 통해 Redis 데이터를 실시간으로 대시보드에 전송
- **페이징 및 검색**: 노선 데이터를 페이징 및 검색 기능과 함께 제공
- **관리자 기능**: 관리자 권한으로 노선 및 정류소 데이터 관리
- **OAuth2 인증**: Kakao 및 Google OAuth2를 통한 인증 지원
- **Swagger UI**: API 문서화 및 테스트 지원

---

## 기술 스택
- **백엔드**: Java, Spring Boot
- **데이터베이스**: Redis, JPA (Hibernate)
- **빌드 도구**: Maven
- **API 문서화**: SpringDoc (Swagger)
- **실시간 통신**: WebSocket
- **보안**: Spring Security, OAuth2

---

## 설치 및 실행

### 1. 필수 요구사항
- Java 17 이상
- Maven 3.8 이상
- Redis 서버 실행 중
- 데이터베이스 설정 (JPA 및 Redis)

### 2. 프로젝트 클론
```bash
git clone https://github.com/heeha153/bus2.git
cd bus2
```

### 3. 환경 설정
`src/main/resources/application.properties` 파일을 수정하여 환경 설정을 업데이트합니다:
- Redis 설정 (`spring.data.redis.host`, `spring.data.redis.port`)
- OAuth2 설정 (Kakao, Google)
- 파일 업로드 경로 (`file.upload.found-location`, `file.upload.ad-location`)

### 4. 빌드 및 실행
```bash
mvn clean install
mvn spring-boot:run
```

---

## 주요 API

### 노선 관리
- **GET** `/api/bus/routes`: 전체 노선 조회 (페이징 및 검색 지원)
- **POST** `/api/bus/AddBusRoute`: 새로운 노선 추가
- **PUT** `/api/bus/UpdateRouteUnified/{routeId}`: 노선 정보 수정
- **DELETE** `/api/bus/deleteRoute`: 노선 삭제

### 정류소 관리
- **POST** `/api/bus/AddRouteStopLink`: 경유 정류소 추가
- **POST** `/api/bus/InsertStop`: 정류소 삽입
- **DELETE** `/api/bus/delete-stop`: 정류소 삭제

### 버스 시간표 관리
- **GET** `/api/schedules`: 노선 시간표 조회
- **GET** `/api/lowbus-schedules`: 저상 버스 시간표 조회
- **POST** `/api/schedule/add`: 시간표 삽입
- **PUT** `/api/schedule/modify`: 시간표 수정
- **DELETE** `/api/schedule/delete`: 시간표 삭제

### 노선 맵 정류장 관리
- **GET** `/api/schedule-headers`: 노선 지정 정류장 조회
- **POST** `/api/schedule-headers`: 노선 지정 정류장 추가
- **PUT** `/api/schedule-headers/{id}`: 노선 지정 정류장 수정
- **DELETE** `/api/schedule-headers/{id}`: 노선 지정 정류장 삭제

### 버스 요금 관리
- **GET** `/api/fares`: 버스 요금 조회
- **POST** `/api/fares`: 버스 요금 추가
- **PUT** `/api/fares/{id}`: 버스 요금 수정
- **DELETE** `/api/fares/{id}`: 버스 요금 삭제

### 실시간 데이터
- WebSocket URL: `ws://localhost:8081/dashboard`

---

## 개발 환경
- **IDE**: IntelliJ IDEA 2024.3.5
- **운영 체제**: Windows
- **빌드 도구**: Maven

---

## 기여
1. 이슈를 생성하여 버그 또는 새로운 기능 요청
2. Pull Request를 통해 코드 기여

---

## 라이선스
이 프로젝트는 MIT 라이선스를 따릅니다. [LICENSE](LICENSE) 파일을 참조하세요.
