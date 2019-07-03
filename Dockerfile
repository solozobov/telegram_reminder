FROM openjdk:8u191-jre-alpine3.8
MAINTAINER Andrey Solozobov <Andrei.Solozobov@gmail.com>

WORKDIR /usr/src/remember
COPY target/remember-1.0-SNAPSHOT.jar /usr/src/remember/remember.jar
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "remember.jar", "--spring.config.location=file:/run/secrets/application.properties"]
