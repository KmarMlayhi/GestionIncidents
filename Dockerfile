# =====  BUILD : compile + package =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build du jar (sans tests pour aller plus vite en docker build)
RUN mvn -B clean package -DskipTests

# =====  RUN : exécute le jar =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
