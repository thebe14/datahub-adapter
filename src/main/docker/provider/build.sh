#!/bin/bash

echo "Building the DataHub bridge..."
cd ../../../..
mvn package -DskipTests
cd src/main/docker/provider

echo "Building the containers..."
sudo docker-compose up -d --build --remove-orphans
