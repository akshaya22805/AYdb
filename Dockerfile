# Use official Java image
FROM openjdk:17-jdk-slim

# Create app folder
WORKDIR /app

# Copy all project files
COPY . /app

# Compile all Java files
RUN javac *.java

# Expose port
EXPOSE 8080

# Start your server
CMD ["java", "simpleServer"]
