#!/bin/sh

# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

mkdir -p generated/src/main/java
mkdir -p generated/src/main/kotlin
protoc -I=../proto/prl --java_out=generated/src/main/java --kotlin_out=generated/src/main/kotlin ../proto/prl/*.proto

