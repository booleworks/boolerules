# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.source=https://github.com/booleworks/boolerules
RUN mkdir /opt/boolerules
COPY target/boolerules-jar-with-dependencies.jar /opt/boolerules
CMD java -jar /opt/boolerules/boolerules-jar-with-dependencies.jar
EXPOSE 7070
