# Ubuntu 22.04 기반의 openjdk 이미지 (슬림하지 않음)
FROM ubuntu:22.04

# 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk net-tools iputils-ping vim vim && \
    apt-get clean

# JAVA 환경 변수 (Ubuntu에서 openjdk 설치 경로는 아래와 같음)
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# 앱 디렉터리 설정
ENV APP_HOME=/apps
WORKDIR $APP_HOME

# 빌드된 JAR 복사
ARG JAR_FILE_PATH=target/*.jar
COPY ${JAR_FILE_PATH} app.jar

# 환경 변수
ENV API_SECRET_KEY=place_holder

# 포트 노출
EXPOSE 8094 16379 

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

