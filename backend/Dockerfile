# =========================
# 1) Build (Maven)
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache de dependências
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia apenas o que interessa (garanta .dockerignore, abaixo)
COPY src ./src

# Build COM testes (JUnit / Spring Boot)
RUN mvn -B clean verify

# Pega o JAR "boot" (evita pegar original-*.jar)
RUN JAR="$(ls -1 target/*-SNAPSHOT.jar target/*.jar 2>/dev/null | grep -v 'original-' | head -n 1)" \
 && test -n "$JAR" \
 && echo "JAR gerado: $JAR" \
 && cp "$JAR" /workspace/app.jar

# =========================
# 2) Runtime (JRE leve)
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -ms /bin/bash appuser \
 && mkdir -p /app/logs \
 && chown -R appuser:appuser /app

# copia só o jar final (não leva target/)
COPY --from=build /workspace/app.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

USER appuser
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
