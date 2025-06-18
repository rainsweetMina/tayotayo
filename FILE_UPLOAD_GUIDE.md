# 파일 업로드 가이드

## 개요
이 프로젝트는 JWT 인증 기반의 안전한 파일 업로드 시스템을 제공합니다. 광고 이미지 업로드를 위한 기능이 구현되어 있습니다.

## 주요 기능

### 1. 보안 기능
- **JWT 인증**: 모든 파일 업로드 API는 JWT 토큰 인증 필요
- **권한 검사**: ADMIN 권한이 있는 사용자만 파일 업로드 가능
- **파일 타입 검증**: 이미지 파일만 업로드 허용
- **파일 크기 제한**: 최대 20MB까지 허용
- **UUID 파일명**: 보안을 위해 원본 파일명 대신 UUID 사용

### 2. 지원 파일 형식
- **이미지 타입**: JPEG, JPG, PNG, GIF, WebP
- **Content-Type**: image/jpeg, image/jpg, image/png, image/gif, image/webp
- **확장자**: .jpg, .jpeg, .png, .gif, .webp

## API 엔드포인트

### 1. 광고 등록 (파일 업로드 포함)
```http
POST /api/ad
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}

FormData:
- dto: JSON (AdRequestDTO)
- image: 파일
```

**AdRequestDTO 예시:**
```json
{
    "title": "광고 제목",
    "linkUrl": "https://example.com",
    "startDateTime": "2024-01-01T00:00:00",
    "endDateTime": "2024-12-31T23:59:59",
    "showPopup": true,
    "companyId": 1
}
```

### 2. 광고 수정 (파일 업로드 선택적)
```http
PUT /api/ad/{id}
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}

FormData:
- dto: JSON (AdUpdateRequestDTO)
- image: 파일 (선택사항)
```

### 3. 파일 업로드 테스트
```http
POST /api/ad/test-upload
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}

FormData:
- file: 파일
```

## 클라이언트 사용 예시

### JavaScript (Fetch API)
```javascript
// JWT 토큰 설정
const token = 'your-jwt-token';

// 광고 등록 (파일 업로드 포함)
async function createAdWithImage(adData, imageFile) {
    const formData = new FormData();
    
    // JSON DTO 추가
    formData.append('dto', JSON.stringify(adData));
    
    // 이미지 파일 추가
    formData.append('image', imageFile);
    
    const response = await fetch('/api/ad', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        body: formData
    });
    
    if (response.ok) {
        const result = await response.json();
        console.log('광고 등록 성공:', result);
        return result;
    } else {
        const error = await response.json();
        console.error('광고 등록 실패:', error);
        throw new Error(error.error);
    }
}

// 사용 예시
const adData = {
    title: "새로운 광고",
    linkUrl: "https://example.com",
    startDateTime: "2024-01-01T00:00:00",
    endDateTime: "2024-12-31T23:59:59",
    showPopup: true,
    companyId: 1
};

const imageFile = document.getElementById('imageInput').files[0];
createAdWithImage(adData, imageFile);
```

### HTML 폼 예시
```html
<form id="adForm" enctype="multipart/form-data">
    <input type="text" name="title" placeholder="광고 제목" required>
    <input type="url" name="linkUrl" placeholder="링크 URL" required>
    <input type="datetime-local" name="startDateTime" required>
    <input type="datetime-local" name="endDateTime" required>
    <input type="checkbox" name="showPopup"> 팝업 표시
    <select name="companyId" required>
        <option value="">광고회사 선택</option>
        <option value="1">회사 A</option>
        <option value="2">회사 B</option>
    </select>
    <input type="file" name="image" accept="image/*" required>
    <button type="submit">광고 등록</button>
</form>

<script>
document.getElementById('adForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const imageFile = formData.get('image');
    
    const adData = {
        title: formData.get('title'),
        linkUrl: formData.get('linkUrl'),
        startDateTime: formData.get('startDateTime'),
        endDateTime: formData.get('endDateTime'),
        showPopup: formData.get('showPopup') === 'on',
        companyId: parseInt(formData.get('companyId'))
    };
    
    try {
        await createAdWithImage(adData, imageFile);
        alert('광고가 성공적으로 등록되었습니다.');
    } catch (error) {
        alert('광고 등록에 실패했습니다: ' + error.message);
    }
});
</script>
```

## 에러 처리

### 일반적인 에러 응답
```json
{
    "error": "에러 메시지"
}
```

### 주요 에러 케이스
- **401 Unauthorized**: JWT 토큰이 없거나 유효하지 않음
- **403 Forbidden**: ADMIN 권한이 없음
- **400 Bad Request**: 
  - 파일이 비어있음
  - 파일 크기가 20MB 초과
  - 지원하지 않는 파일 타입
  - 필수 필드 누락

## 파일 저장 구조

```
uploads/
└── ad/
    ├── uuid1.jpg
    ├── uuid2.png
    └── uuid3.gif
```

- 파일은 `uploads/ad/` 디렉토리에 저장
- 파일명은 UUID로 생성되어 보안 강화
- DB에는 `ad/uuid.jpg` 형태의 상대 경로 저장

## 설정

### application.properties
```properties
# 파일 업로드 설정
file.upload.ad-location=/uploads/ad/
file.url-prefix=/uploads/ad/

# Spring Boot 파일 업로드 설정
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=25MB
```

## 보안 고려사항

1. **인증**: 모든 파일 업로드 API는 JWT 토큰 필요
2. **권한**: ADMIN 권한이 있는 사용자만 업로드 가능
3. **파일 검증**: 
   - 파일 타입 검사
   - 파일 크기 제한
   - 확장자 검사
4. **파일명 보안**: UUID 사용으로 예측 불가능한 파일명
5. **디렉토리 트래버설 방지**: 상대 경로 사용 제한

## 로깅

파일 업로드 관련 로그:
- `INFO`: 파일 저장 성공, 디렉토리 생성
- `WARN`: 파일 삭제 실패, 잘못된 요청
- `ERROR`: 파일 저장 실패, 시스템 오류

## 테스트

### Swagger UI에서 테스트
1. `/swagger-ui` 접속
2. JWT 토큰 설정 (Authorize 버튼)
3. `/api/ad/test-upload` 엔드포인트로 파일 업로드 테스트
4. `/api/ad` 엔드포인트로 실제 광고 등록 테스트

### cURL 예시
```bash
# 파일 업로드 테스트
curl -X POST "http://localhost:8081/api/ad/test-upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"

# 광고 등록
curl -X POST "http://localhost:8081/api/ad" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "dto={\"title\":\"테스트 광고\",\"linkUrl\":\"https://example.com\",\"startDateTime\":\"2024-01-01T00:00:00\",\"endDateTime\":\"2024-12-31T23:59:59\",\"showPopup\":true,\"companyId\":1}" \
  -F "image=@/path/to/image.jpg"
``` 