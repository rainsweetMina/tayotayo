# Docker 권한 설정 가이드

## 문제
```
permission denied while trying to connect to the Docker daemon socket
```

## 해결 방법

### 1. Docker 그룹에 사용자 추가
```bash
sudo usermod -aG docker $USER
```

### 2. 새 터미널에서 로그인
```bash
# 현재 터미널을 닫고 새 터미널 열기
# 또는 다음 명령어로 그룹 적용
newgrp docker
```

### 3. 권한 확인
```bash
docker ps
```

## 대안 방법

### 방법 1: sudo 사용 (임시)
```bash
sudo docker-compose up -d
```

### 방법 2: 빌드만 실행
```bash
./deploy-production-no-docker.sh
```

## 완전한 해결 후 사용
```bash
./deploy-production.sh
``` 