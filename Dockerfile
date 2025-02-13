# 빌드 이미지
FROM gradle:8.11-jdk17 AS build

WORKDIR /app

# Gradle 파일 복사
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
COPY src /app/src

# Gradlew에 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle 빌드
RUN ./gradlew clean build --no-daemon --refresh-dependencies

# 런타임 이미지
FROM ubuntu:24.04

# 환경 변수 설정 (비대화식 설치)
ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=ko_KR.UTF-8
ENV LANGUAGE=ko_KR:ko
ENV LC_ALL=ko_KR.UTF-8
ENV TZ=Asia/Seoul

# MySQL 및 OpenJDK 설치, 로케일 추가
RUN apt-get update && apt-get install -y \
    mysql-server \
    openjdk-17-jdk \
    locales && \
    locale-gen ko_KR.UTF-8 && \
    apt-get clean

# MySQL 사용자의 홈 디렉토리 수정
RUN usermod -d /var/lib/mysql mysql

# MySQL 데이터 디렉토리 초기화 및 포트 변경
RUN echo "\n[mysqld]\nport=53306" >> /etc/mysql/mysql.cnf

# MySQL 초기화 및 데이터베이스 설정
RUN service mysql start && sleep 10 && \
    mysql -e "CREATE DATABASE IF NOT EXISTS alarm_it;" && \
    mysql -e "CREATE USER IF NOT EXISTS 'ryuoo0'@'%' IDENTIFIED BY 'rlgus!';" && \
    mysql -e "GRANT ALL PRIVILEGES ON alarm_it.* TO 'ryuoo0'@'%';" && \
    mysql -e "FLUSH PRIVILEGES;" && \
    service mysql stop

# 작업 디렉토리 설정
WORKDIR /app

# 빌드한 .jar 파일 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 포트 개방
EXPOSE 58080

# MySQL 및 Spring Boot 실행
CMD ["sh", "-c", "service mysql start && sleep 5 && java -Dfile.encoding=UTF-8 -jar /app/app.jar --server.port=58080"]
