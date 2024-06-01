#!/bin/sh

# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

threads="${1:-1}"

mvn clean package -DskipTests &&
  INSTANCE="localhost" \
  PORT="7070" \
  REDIS_URL="http://localhost:6379" \
  NUM_THREADS="$threads" \
  java -jar target/boolerules-jar-with-dependencies.jar
