# Этап 1: Сборка приложения (builder stage)
# Используем официальный образ Java 17 с JDK (для компиляции)
FROM eclipse-temurin:17-jdk-alpine as builder

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем файлы Maven Wrapper
# Это позволяет использовать точно ту же версию Maven, что и локально
COPY ../mvnw .
COPY ../.mvn .mvn

# Копируем файл с зависимостями
# Docker кеширует слои, поэтому если pom.xml не изменился,
# следующая команда возьмется из кеша
COPY ../pom.xml .

# Загружаем все зависимости проекта
# Это отдельный шаг для оптимизации кеширования
RUN ./mvnw dependency:go-offline

# Копируем исходный код
# Это делаем последним, так как код меняется чаще всего
COPY ../src src

# Собираем JAR файл, пропуская тесты для ускорения сборки
RUN ./mvnw package -DskipTests

# Этап 2: Запуск приложения (runtime stage)
# Используем облегченный образ только с JRE (без JDK)
FROM eclipse-temurin:17-jre-alpine

# Создаём пользователя для безопасности (не root)
# Это best practice - приложение не должно работать от root
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Переключаемся на созданного пользователя
USER spring:spring

# Копируем собранный JAR из builder stage
# --from=builder указывает на первый stage
COPY --from=builder /app/target/*.jar app.jar

# Указываем команду запуска
# java -jar запускает наше Spring Boot приложение
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Можно добавить дополнительные параметры JVM
# ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/app.jar"]