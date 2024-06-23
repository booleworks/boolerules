#!/bin/sh

# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

if [ -z "$1" ]
  then
    echo "Please provide the version as first (and only) argument"
    exit 1
fi

tag="ghcr.io/booleworks/boolerules-showcase:${1}"

yarn build && docker buildx create --use && docker buildx build . --platform linux/amd64,linux/arm64,linux/arm/v7 --push -t "$tag"
