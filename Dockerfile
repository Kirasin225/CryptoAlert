FROM gradle:jdk21-jammy AS build
WORKDIR /app
COPY . .
# Собираем jar, пропуская тесты (чтобы быстрее)
RUN ./gradlew bootJar -x test --no-daemon

# Этап 2: Запуск (Run)
# Используем легкий Linux (Alpine) с Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Копируем только собранный jar из первого этапа
COPY --from=build /app/build/libs/*.jar app.jar

# Говорим, что приложение слушает порт 8080
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]