/**
 * 광고 업로드 예시
 * JWT 토큰을 사용하여 파일 업로드를 수행합니다.
 */

// 광고 등록 함수
async function createAdWithImage(adData, imageFile) {
    try {
        // 토큰 유효성 확인
        if (!tokenManager.hasValidToken()) {
            throw new Error('유효한 토큰이 없습니다. 로그인이 필요합니다.');
        }

        const formData = new FormData();
        
        // JSON DTO 추가
        formData.append('dto', JSON.stringify(adData));
        
        // 이미지 파일 추가
        formData.append('image', imageFile);
        
        // 토큰 매니저의 authenticatedFetch 사용
        const response = await tokenManager.authenticatedFetch('/api/ad', {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('광고 등록 성공:', result);
            return result;
        } else {
            // 400 에러 처리
            if (response.status === 400) {
                const errorData = await response.json();
                console.error('400 에러 - 요청 데이터 오류:', errorData);
                
                // 구체적인 에러 메시지 처리
                if (errorData.error) {
                    throw new Error(errorData.error);
                } else {
                    throw new Error('요청 데이터가 올바르지 않습니다.');
                }
            }
            
            // 401 에러 처리
            if (response.status === 401) {
                console.error('401 에러 - 인증 실패');
                throw new Error('인증이 필요합니다. 다시 로그인해주세요.');
            }
            
            // 403 에러 처리
            if (response.status === 403) {
                console.error('403 에러 - 권한 없음');
                throw new Error('이 작업을 수행할 권한이 없습니다.');
            }
            
            // 기타 에러 처리
            const errorData = await response.json().catch(() => ({}));
            console.error('API 요청 실패:', response.status, errorData);
            throw new Error(errorData.error || `요청 실패 (${response.status})`);
        }
    } catch (error) {
        console.error('광고 등록 오류:', error);
        throw error;
    }
}

// 광고 수정 함수
async function updateAdWithImage(adId, adData, imageFile = null) {
    try {
        if (!tokenManager.hasValidToken()) {
            throw new Error('유효한 토큰이 없습니다. 로그인이 필요합니다.');
        }

        const formData = new FormData();
        formData.append('dto', JSON.stringify(adData));
        
        if (imageFile) {
            formData.append('image', imageFile);
        }
        
        const response = await tokenManager.authenticatedFetch(`/api/ad/${adId}`, {
            method: 'PUT',
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('광고 수정 성공:', result);
            return result;
        } else {
            // 400 에러 처리
            if (response.status === 400) {
                const errorData = await response.json();
                console.error('400 에러 - 요청 데이터 오류:', errorData);
                
                if (errorData.error) {
                    throw new Error(errorData.error);
                } else {
                    throw new Error('요청 데이터가 올바르지 않습니다.');
                }
            }
            
            // 404 에러 처리
            if (response.status === 404) {
                throw new Error('수정할 광고를 찾을 수 없습니다.');
            }
            
            // 기타 에러 처리
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `광고 수정에 실패했습니다. (${response.status})`);
        }
    } catch (error) {
        console.error('광고 수정 오류:', error);
        throw error;
    }
}

// 파일 업로드 테스트 함수
async function testFileUpload(file) {
    try {
        if (!tokenManager.hasValidToken()) {
            throw new Error('유효한 토큰이 없습니다. 로그인이 필요합니다.');
        }

        const formData = new FormData();
        formData.append('file', file);
        
        const response = await tokenManager.authenticatedFetch('/api/ad/test-upload', {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('파일 업로드 테스트 성공:', result);
            return result;
        } else {
            // 400 에러 처리
            if (response.status === 400) {
                const errorData = await response.json();
                console.error('400 에러 - 파일 업로드 오류:', errorData);
                
                if (errorData.error) {
                    throw new Error(errorData.error);
                } else {
                    throw new Error('파일 업로드에 실패했습니다.');
                }
            }
            
            // 기타 에러 처리
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `파일 업로드 테스트에 실패했습니다. (${response.status})`);
        }
    } catch (error) {
        console.error('파일 업로드 테스트 오류:', error);
        throw error;
    }
}

// 광고 목록 조회 함수
async function getAds() {
    try {
        const response = await tokenManager.authenticatedFetch('/api/ad');
        
        if (response.ok) {
            const ads = await response.json();
            console.log('광고 목록 조회 성공:', ads);
            return ads;
        } else {
            // 401 에러 처리
            if (response.status === 401) {
                throw new Error('인증이 필요합니다. 다시 로그인해주세요.');
            }
            
            // 기타 에러 처리
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `광고 목록 조회에 실패했습니다. (${response.status})`);
        }
    } catch (error) {
        console.error('광고 목록 조회 오류:', error);
        throw error;
    }
}

// HTML 폼 이벤트 핸들러 예시
function setupAdForm() {
    const form = document.getElementById('adForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        try {
            const formData = new FormData(form);
            const imageFile = formData.get('image');
            
            // 폼 데이터를 객체로 변환
            const adData = {
                title: formData.get('title'),
                linkUrl: formData.get('linkUrl'),
                startDateTime: formData.get('startDateTime'),
                endDateTime: formData.get('endDateTime'),
                showPopup: formData.get('showPopup') === 'on',
                companyId: parseInt(formData.get('companyId'))
            };
            
            // 유효성 검사
            if (!adData.title || !adData.linkUrl || !adData.startDateTime || !adData.endDateTime) {
                alert('모든 필수 필드를 입력해주세요.');
                return;
            }
            
            if (!imageFile || imageFile.size === 0) {
                alert('이미지 파일을 선택해주세요.');
                return;
            }
            
            // 로딩 표시
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.textContent = '업로드 중...';
            submitBtn.disabled = true;
            
            // 광고 등록
            await createAdWithImage(adData, imageFile);
            
            alert('광고가 성공적으로 등록되었습니다.');
            form.reset();
            
        } catch (error) {
            // 구체적인 에러 메시지 표시
            let errorMessage = error.message;
            
            // 400 에러 관련 구체적인 메시지
            if (error.message.includes('파일 크기')) {
                errorMessage = '파일 크기가 20MB를 초과했습니다. 더 작은 파일을 선택해주세요.';
            } else if (error.message.includes('파일 타입')) {
                errorMessage = '지원하지 않는 파일 형식입니다. JPG, PNG, GIF, WebP 파일만 업로드 가능합니다.';
            } else if (error.message.includes('인증')) {
                errorMessage = '로그인이 필요합니다. 다시 로그인해주세요.';
            } else if (error.message.includes('권한')) {
                errorMessage = '이 작업을 수행할 권한이 없습니다. 관리자에게 문의하세요.';
            }
            
            alert('광고 등록에 실패했습니다: ' + errorMessage);
        } finally {
            // 버튼 상태 복원
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        }
    });
}

// 파일 선택 시 미리보기
function setupImagePreview() {
    const imageInput = document.getElementById('imageInput');
    const preview = document.getElementById('imagePreview');
    
    if (!imageInput || !preview) return;
    
    imageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // 파일 유효성 검사
            if (!file.type.startsWith('image/')) {
                alert('이미지 파일만 선택할 수 있습니다.');
                this.value = '';
                preview.style.display = 'none';
                return;
            }
            
            if (file.size > 20 * 1024 * 1024) {
                alert('파일 크기는 20MB를 초과할 수 없습니다.');
                this.value = '';
                preview.style.display = 'none';
                return;
            }
            
            // 미리보기 표시
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            };
            reader.readAsDataURL(file);
        } else {
            preview.style.display = 'none';
        }
    });
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 토큰 매니저 초기화 (jwt-token-manager.js에서 자동으로 실행됨)
    
    // 폼 설정
    setupAdForm();
    setupImagePreview();
    
    // 토큰 상태 확인
    if (!tokenManager.hasValidToken()) {
        console.warn('유효한 토큰이 없습니다. 로그인이 필요합니다.');
        // 필요시 로그인 페이지로 리다이렉트
        // window.location.href = '/auth/login';
    } else {
        console.log('토큰이 유효합니다.');
    }
});

// 전역 함수로 노출
window.createAdWithImage = createAdWithImage;
window.updateAdWithImage = updateAdWithImage;
window.testFileUpload = testFileUpload;
window.getAds = getAds; 