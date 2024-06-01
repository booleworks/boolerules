#!/bin/sh

# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

# if multi-platform build does not work, check https://docs.docker.com/build/building/multi-platform
# TL;DR try installing qemu and run `docker run --privileged --rm tonistiigi/binfmt --install all`

if [ -z "$1" ]
  then
    echo "Please provide the version as first (and only) argument"
    exit 1
fi

tag="ghcr.io/booleworks/boolerules:${1}"

mvn clean package -DskipTests && docker buildx create --use && docker buildx build . --platform linux/amd64,linux/arm64,linux/arm/v7 --push -t "$tag"
