FROM openjdk:8u111-jdk-alpine

RUN apk add --no-cache git apache-ant

WORKDIR /usr/src/ms-post-reformatter/
ADD . .
RUN ant -f build.xml

ENTRYPOINT ["./docker-start.sh"]
