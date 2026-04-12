# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copia o pom.xml e baixa as dependências em cache (layer separado)
COPY bip-teste-integrado/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B -q

# Copia o source e empacota
COPY bip-teste-integrado/src ./src
RUN mvn package -DskipTests -B -q

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]