# ---- Stage 1: build ----
FROM amazoncorretto:21 AS builder

WORKDIR /app

# Amazon Corretto 21 (base Amazon Linux 2023) não vem com 'tar' instalado.
# O mvnw precisa dele para descompactar a distribuição do Maven que baixa.
RUN dnf install -y tar gzip && dnf clean all

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: runtime ----
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50.0", "-XX:InitialRAMPercentage=25.0", "-jar", "app.jar"]