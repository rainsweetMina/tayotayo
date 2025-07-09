#!/bin/bash

echo "🚀 프론트엔드 Production 빌드 시작..."

# 프론트엔드 디렉토리로 이동
cd frontendtayotayo

# 기존 빌드 파일 삭제
echo "🗑️ 기존 빌드 파일 삭제..."
rm -rf dist

# 의존성 설치
echo "📦 의존성 설치..."
npm install

# Production 빌드
echo "🔨 Production 빌드 실행..."
npm run build

# 빌드 결과 확인
if [ -d "dist" ]; then
    echo "✅ 빌드 성공! dist 폴더가 생성되었습니다."
    echo "📁 빌드된 파일들:"
    ls -la dist/
else
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "🎉 프론트엔드 빌드 완료!" 