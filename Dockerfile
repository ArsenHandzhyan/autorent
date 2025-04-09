# Stage 1: Сборка приложения
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Копируем скрипт сборки, зависимости и исходные файлы
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Делаем скрипт mvnw исполняемым и выполняем сборку с пропуском тестов
RUN chmod +x ./mvnw && \
    ./mvnw install -DskipTests && \
    mkdir -p target/dependency && \
    (cd target/dependency && jar -xf ../*.jar)

# Stage 2: Запуск приложения
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Копируем собранные файлы из первой стадии
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java","-cp","app:app/lib/*","ru.anapa.autorent.AutorentApplication"]