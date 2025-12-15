# =========================
# 1) Build (Maven)
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copia apenas o pom primeiro para cachear dependências
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Agora copia o resto e empacota
COPY src ./src
# (opcional) se tiver módulos extra, copie aqui também
# COPY modules ./modules

# Perfil de produção por padrão; mude com --build-arg SPRING_PROFILE=dev
ARG SPRING_PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

# Gera o fat JAR (spring-boot:repackage)
RUN mvn -B -q clean package -DskipTests

# Descobre o jar final (qualquer *.jar em target)
RUN test -f target/*.jar && echo "JAR gerado:" && ls -l target/*.jar

# =========================
# 2) Runtime (JRE leve)
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Cria usuário não-root por segurança
RUN useradd -ms /bin/bash appuser
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Copia o jar do estágio anterior
# Ajuste o nome se você quiser travar num jar específico
COPY --from=build /workspace/target/*.jar /app/app.jar

# Variáveis de runtime (ajuste conforme necessidade)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=prod
# Exemplos de configs via env (substitua pelos teus):
# ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/surfguru
# ENV SPRING_DATASOURCE_USERNAME=surfguru
# ENV SPRING_DATASOURCE_PASSWORD=surfguru
# ENV SURF_LLM_PROVIDER=openai
# ENV SURF_LLM_API_KEY=replace-me

EXPOSE 8080

# Healthcheck simples (ajuste endpoint se preferir)
HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
