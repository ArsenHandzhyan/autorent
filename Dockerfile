# Stage 1: Сборка приложения
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Устанавливаем Maven
RUN apk add --no-cache maven

# Копируем pom.xml и исходный код
COPY pom.xml .
COPY src src

# Собираем приложение
RUN mvn package -DskipTests

# Stage 2: Запуск приложения
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Копируем JAR-файл из первой стадии
COPY --from=build /workspace/app/target/*.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение с профилем dev (облачная база)
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","/app.jar"]