# SPDX-License-Identifier: MIT
# Copyright 2023 BooleWorks GmbH

FROM eclipse-temurin:17-jre
RUN mkdir /opt/boolerules
COPY target/boolerules-jar-with-dependencies.jar /opt/boolerules
CMD java -jar /opt/boolerules/boolerules-jar-with-dependencies.jar
EXPOSE 7070
