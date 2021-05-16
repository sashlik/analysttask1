FROM openjdk:11
RUN mkdir /app
WORKDIR /app
COPY target/analysttask1.jar /app/analysttask1.jar
ENTRYPOINT ["java", "-jar", "/app/analysttask1.jar"]