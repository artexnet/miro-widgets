FROM openjdk:11-jre-slim

COPY ./target/miro-hw-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch miro-hw-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","miro-hw-0.0.1-SNAPSHOT.jar"]