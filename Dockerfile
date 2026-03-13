# Этап 1: Сборка
FROM gradle:9-jdk25-corretto AS build
WORKDIR /app

# Копирование файлов Gradle и исходного кода
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle gradle
COPY gradlew .
COPY src src

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Сборка приложения
RUN ./gradlew bootJar

# Этап 2: Запуск
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=build /app/build/libs/*.jar minesweeper.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "minesweeper.jar"]