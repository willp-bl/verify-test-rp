#!/usr/bin/env bash

cd $(dirname "${BASH_SOURCE[0]}")

./gradlew clean
./gradlew distZip -Pversion=local
docker build -t test-rp:latest -f run.Dockerfile . 2>&1
echo "test-rp:latest"
