# Use official Java image
FROM eclipse-temurin:17-jdk-jammy

# Create app folder
WORKDIR /app

# Install wget to download dependencies
RUN apt-get update && apt-get install -y wget

# Download the org.json library
RUN wget https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar -O /app/json.jar

# Copy all Java files
COPY . /app

# Compile with classpath including json.jar
RUN javac -cp .:json.jar *.java

# Expose port
EXPOSE 8080

# Run server
CMD ["java", "-cp", ".:json.jar", "simpleServer"]
