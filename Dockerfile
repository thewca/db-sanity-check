FROM azul/zulu-openjdk-alpine:11-jre
COPY build/libs/db-sanity-check.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
