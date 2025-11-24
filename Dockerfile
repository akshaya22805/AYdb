# Use official Eclipse Temurin Java 17 image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . /app

RUN javac *.java

EXPOSE 8080

CMD ["java", "simpleServer"]
