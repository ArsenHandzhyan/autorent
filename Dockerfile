# Stage 1: Сборка приложения
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Копируем скрипт сборки, зависимости и исходные файлы
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Загружаем зависимости отдельно для кэширования слоев
RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src src
RUN ./mvnw package -DskipTests && \
    mkdir -p target/dependency && \
    (cd target/dependency && jar -xf ../*.jar)

# Stage 2: Запуск приложения
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp
VOLUME /var/log/autorent

# Установка необходимых утилит для мониторинга
RUN apk add --no-cache curl

# Копируем собранные файлы из первой стадии
COPY --from=build /workspace/app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/target/dependency/BOOT-INF/classes /app

# Настройка JVM для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT exec java $JAVA_OPTS -cp app:app/lib/* ru.anapa.autorent.AutorentApplication