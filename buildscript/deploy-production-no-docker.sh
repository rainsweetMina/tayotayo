#!/bin/bash

echo "🚀 Production 배포 시작 (Docker 권한 없음)..."

# 1. 프론트엔드 빌드
echo "📦 1단계: 프론트엔드 빌드"
./build-frontend.sh

if [ $? -ne 0 ]; then
    echo "❌ 프론트엔드 빌드 실패!"
    exit 1
fi

# 2. 백엔드 빌드 (Maven 사용)
echo "🔨 2단계: 백엔드 빌드 (Maven)"
cd tayotayo
./mvnw clean package -DskipTests
cd ..

# 3. 빌드 결과 확인
echo "✅ 3단계: 빌드 결과 확인"
if [ -f "tayotayo/target/bus2-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ 백엔드 JAR 파일 생성 완료"
    ls -la tayotayo/target/bus2-0.0.1-SNAPSHOT.jar
else
    echo "❌ 백엔드 JAR 파일 생성 실패!"
    exit 1
fi

if [ -d "frontendtayotayo/dist" ]; then
    echo "✅ 프론트엔드 빌드 완료"
    ls -la frontendtayotayo/dist/
else
    echo "❌ 프론트엔드 빌드 실패!"
    exit 1
fi

echo "🎉 빌드 완료!"
echo ""
echo "📋 다음 단계:"
echo "1. Docker 권한 설정: sudo usermod -aG docker \$USER"
echo "2. 새 터미널에서 로그인 후: ./deploy-production.sh"
echo ""
echo "또는 수동으로 컨테이너 실행:"
echo "sudo docker-compose up -d" 