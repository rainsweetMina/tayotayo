#!/bin/bash

echo "🚀 Production 배포 시작..."

# 1. 프론트엔드 빌드
echo "📦 1단계: 프론트엔드 빌드"
./build-frontend.sh

if [ $? -ne 0 ]; then
    echo "❌ 프론트엔드 빌드 실패!"
    exit 1
fi

# 2. 기존 컨테이너 중지 및 삭제
echo "🛑 2단계: 기존 컨테이너 정리"
sudo docker-compose down

# 3. 백엔드 빌드 (Maven 사용)
echo "🔨 3단계: 백엔드 빌드 (Maven)"
cd tayotayo
./mvnw clean package -DskipTests
cd ..

# 4. 컨테이너 시작
echo "🚀 4단계: 컨테이너 시작"
sudo docker-compose up -d

# 5. 상태 확인
echo "✅ 5단계: 서비스 상태 확인"
sleep 10
sudo docker-compose ps

echo "🎉 Production 배포 완료!"
echo "🌐 접속 URL: https://docs.yi.or.kr:8096"
echo "📊 백엔드 상태: https://docs.yi.or.kr:8096/actuator/health" 