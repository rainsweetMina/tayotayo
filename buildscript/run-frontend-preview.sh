#!/bin/bash

echo "🚀 프론트엔드 빌드 파일 미리보기 시작..."

# 프론트엔드 디렉토리로 이동
cd frontendtayotayo

# 빌드 파일이 있는지 확인
if [ ! -d "dist" ]; then
    echo "❌ dist 폴더가 없습니다. 먼저 빌드를 실행하세요:"
    echo "   ./build-frontend.sh"
    exit 1
fi

echo "📁 빌드된 파일 확인:"
ls -la dist/

echo "🌐 프론트엔드 미리보기 서버 시작..."
echo "📍 접속 URL: https://docs.yi.or.kr:15173"
echo "⚠️  백엔드 API는 https://docs.yi.or.kr:8096 에서 실행되어야 합니다."

# 미리보기 서버 시작
npm run preview 