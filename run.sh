#!/bin/bash
mvn clean package -Dmaven.test.skip
docker-compose down
docker image rm dht
docker-compose up -d --scale app1=2