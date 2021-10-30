@echo off
echo Building the DataHub bridge...
cd ../../../..
call mvnw package -DskipTests
cd src/main/docker/provider

echo Building the containers...
docker-compose up -d --build --remove-orphans
