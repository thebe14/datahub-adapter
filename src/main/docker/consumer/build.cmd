@echo off
echo Building consumer containers...
docker-compose up -d --build  --remove-orphans
