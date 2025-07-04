#!/usr/bin/bash
# 소스 수정 후 Docker 이미지까지 완전히 반영하는 스크립트

sudo mvn clean
rm -rf target/*.jar
mvn clean package -DskipTests

sudo docker-compose up
