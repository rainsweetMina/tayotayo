#/usr/bin/bash
#소수 수정하고 실행하면 수정된것 적용됨.
sudo mvn clean
./mvnw package -DskipTests
sudo docker-compose up
