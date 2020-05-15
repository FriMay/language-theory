FROM java:8

COPY build/libs/arbina-api-app.jar /usr/local/arbina-api-app.jar

WORKDIR /usr/local

ENTRYPOINT ["java", "-jar", "arbina-api-app.jar"]
