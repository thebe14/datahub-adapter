#!/bin/bash

echo "Building consumer containers..."
sudo docker-compose up -d --build --remove-orphans
