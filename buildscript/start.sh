#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 메뉴 표시 함수
show_menu() {
    echo ""
    echo -e "${BLUE}🚀 TayoTayo 프로젝트 Docker Compose 시작${NC}"
    echo ""
    echo -e "${YELLOW}설치 옵션을 선택하세요:${NC}"
    echo "1) 전체 재설치 (백엔드 + 프론트엔드)"
    echo "2) 백엔드만 재설치"
    echo "3) 프론트엔드만 재설치"
    echo "4) 기존 컨테이너만 재시작 (재설치 없음)"
    echo "5) 종료"
    echo ""
    read -p "선택 (1-5): " choice
}

# 전체 재설치 함수
full_install() {
    echo -e "${GREEN}🔨 전체 재설치 시작...${NC}"
    
    # 필요한 디렉토리 생성
    echo "📁 필요한 디렉토리 생성 중..."
    mkdir -p tayotayo/db/data
    mkdir -p tayotayo/redis/data

    # SSL 인증서 생성 (프론트엔드용)
    echo "🔐 SSL 인증서 생성 중..."
    if [ ! -f frontendtayotayo/localhost+2-key.pem ] || [ ! -f frontendtayotayo/localhost+2.pem ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout frontendtayotayo/localhost+2-key.pem \
            -out frontendtayotayo/localhost+2.pem \
            -subj "/C=KR/ST=Seoul/L=Seoul/O=TayoTayo/OU=IT/CN=docs.yi.or.kr"
        echo "✅ 프론트엔드 SSL 인증서 생성 완료"
    else
        echo "ℹ️  기존 프론트엔드 SSL 인증서 사용"
    fi

    # 기존 컨테이너 정리
    echo "🧹 기존 컨테이너 정리 중..."
    docker-compose down

    # Maven 빌드
    echo "🔨 Maven 빌드 중..."
    cd tayotayo
    mvn clean package -Pprod -DskipTests
    cd ..

    # 전체 이미지 빌드
    echo "🔨 전체 Docker 이미지 빌드 중..."
    docker-compose build --no-cache

    # 컨테이너 시작
    echo "🚀 컨테이너 시작 중..."
    docker-compose up -d
}

# 백엔드만 재설치 함수
backend_install() {
    echo -e "${GREEN}🔨 백엔드만 재설치 시작...${NC}"
    
    # 필요한 디렉토리 생성
    echo "📁 필요한 디렉토리 생성 중..."
    mkdir -p tayotayo/db/data
    mkdir -p tayotayo/redis/data

    # 백엔드 컨테이너만 중지
    echo "🛑 백엔드 컨테이너 중지 중..."
    docker-compose stop backend

    # Maven 빌드
    echo "🔨 Maven 빌드 중..."
    cd tayotayo
    mvn clean package -Pprod -DskipTests
    cd ..

    # 백엔드 이미지만 빌드
    echo "🔨 백엔드 Docker 이미지 빌드 중..."
    docker-compose build --no-cache backend

    # 백엔드 컨테이너 시작
    echo "🚀 백엔드 컨테이너 시작 중..."
    docker-compose up -d backend
}

# 프론트엔드만 재설치 함수
frontend_install() {
    echo -e "${GREEN}🔨 프론트엔드만 재설치 시작...${NC}"
    
    # SSL 인증서 생성 (프론트엔드용)
    echo "🔐 SSL 인증서 생성 중..."
    if [ ! -f frontendtayotayo/localhost+2-key.pem ] || [ ! -f frontendtayotayo/localhost+2.pem ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout frontendtayotayo/localhost+2-key.pem \
            -out frontendtayotayo/localhost+2.pem \
            -subj "/C=KR/ST=Seoul/L=Seoul/O=TayoTayo/OU=IT/CN=docs.yi.or.kr"
        echo "✅ 프론트엔드 SSL 인증서 생성 완료"
    else
        echo "ℹ️  기존 프론트엔드 SSL 인증서 사용"
    fi

    # 프론트엔드 컨테이너만 중지
    echo "🛑 프론트엔드 컨테이너 중지 중..."
    docker-compose stop frontend

    # 프론트엔드 이미지만 빌드
    echo "🔨 프론트엔드 Docker 이미지 빌드 중..."
    docker-compose build --no-cache frontend

    # 프론트엔드 컨테이너 시작
    echo "🚀 프론트엔드 컨테이너 시작 중..."
    docker-compose up -d frontend
}

# 기존 컨테이너만 재시작 함수
restart_only() {
    echo -e "${GREEN}🔄 기존 컨테이너 재시작 중...${NC}"
    
    # 필요한 디렉토리 생성
    echo "📁 필요한 디렉토리 생성 중..."
    mkdir -p tayotayo/db/data
    mkdir -p tayotayo/redis/data

    # 컨테이너 재시작
    echo "🔄 컨테이너 재시작 중..."
    docker-compose restart
}

# 상태 확인 및 정보 표시 함수
show_status() {
    echo ""
    echo -e "${GREEN}📊 컨테이너 상태 확인 중...${NC}"
    docker-compose ps

    echo ""
    echo -e "${GREEN}🎉 TayoTayo 프로젝트가 성공적으로 시작되었습니다!${NC}"
    echo ""
    echo -e "${BLUE}📱 접속 정보:${NC}"
    echo "   - 프론트엔드: https://docs.yi.or.kr:15173"
    echo "   - 백엔드 API: https://docs.yi.or.kr:8094"
    echo "   - 데이터베이스: localhost:23306"
    echo "   - Redis: localhost:16379"
    echo ""
    echo -e "${BLUE}📋 유용한 명령어:${NC}"
    echo "   - 로그 확인: docker-compose logs -f [서비스명]"
    echo "   - 컨테이너 중지: docker-compose down"
    echo "   - 컨테이너 재시작: docker-compose restart"
    echo ""
}

# 메인 실행 로직
main() {
    while true; do
        show_menu
        
        case $choice in
            1)
                full_install
                show_status
                break
                ;;
            2)
                backend_install
                show_status
                break
                ;;
            3)
                frontend_install
                show_status
                break
                ;;
            4)
                restart_only
                show_status
                break
                ;;
            5)
                echo -e "${YELLOW}👋 종료합니다.${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}❌ 잘못된 선택입니다. 1-5 중에서 선택해주세요.${NC}"
                ;;
        esac
    done
}

# 스크립트 실행
main 