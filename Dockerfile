# 빌드 스테이지 - Ubuntu 서버(x86_64)용으로 빌드
FROM --platform=linux/amd64 gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build

# 실행 스테이지 - Ubuntu 서버(x86_64)용으로 빌드
FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]