@echo off
echo Starting Docker containers...
cd docker
docker-compose up -d
echo Docker containers started successfully.
echo.
echo You can stop the containers with: docker-compose down