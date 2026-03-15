# Stage 1: Build with Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run with a slim JRE 21
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar

# Optimizing for Render's 512MB RAM limit
ENV JAVA_OPTS="-Xmx384m -Xms256m -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
