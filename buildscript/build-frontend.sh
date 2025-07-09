#!/bin/bash

echo "🚀 프론트엔드 Production 빌드 시작..."

# Node.js 버전 체크
NODE_VERSION=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "❌ Node.js 버전이 너무 낮습니다. Node.js 16 이상이 필요합니다."
    echo "현재 버전: $(node --version)"
    echo "설치 방법:"
    echo "  curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -"
    echo "  sudo apt-get install -y nodejs"
    exit 1
fi

echo "✅ Node.js 버전 확인: $(node --version)"

# 프론트엔드 디렉토리로 이동
cd frontendtayotayo

# 기존 빌드 파일 삭제
echo "🗑️ 기존 빌드 파일 삭제..."
rm -rf dist

# node_modules 삭제 (의존성 충돌 해결을 위해)
echo "🗑️ 기존 node_modules 삭제..."
rm -rf node_modules package-lock.json

# 의존성 설치 (의존성 충돌 해결)
echo "📦 의존성 설치..."
npm install --legacy-peer-deps

# terser가 필요한 경우 설치 안내
if ! npm list terser > /dev/null 2>&1; then
    echo "⚠️ terser가 설치되지 않았습니다. esbuild를 사용합니다."
    echo "💡 더 나은 압축을 원한다면: npm install --save-dev terser"
fi

# Production 빌드
echo "🔨 Production 빌드 실행..."
npm run build

# 빌드 결과 확인
if [ -d "dist" ]; then
    echo "✅ 빌드 성공! dist 폴더가 생성되었습니다."
    echo "📁 빌드된 파일들:"
    ls -la dist/
    echo "📊 빌드 크기:"
    du -sh dist/
else
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "🎉 프론트엔드 빌드 완료!" 