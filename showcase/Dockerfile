FROM node:21-alpine
LABEL org.opencontainers.image.source=https://github.com/booleworks/boolerules-showcase
RUN mkdir /opt/boolerules-showcase
COPY .output /opt/boolerules-showcase
CMD node /opt/boolerules-showcase/server/index.mjs
EXPOSE 3000
