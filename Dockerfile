# Stage 1: Build da aplicação com Maven usando eclipse-temurin:17-jdk-jammy
FROM eclipse-temurin:17-jdk-jammy AS build
LABEL authors="LucasF"
WORKDIR /app

# Atualiza o apt e instala o Maven
RUN apt-get update && apt-get install -y maven

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código-fonte
COPY src ./src

# Realiza o build e gera o arquivo JAR (pula os testes para acelerar)
RUN mvn clean package -DskipTests

# Stage 2: Imagem final para execução usando eclipse-temurin:17-jre-jammy
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copia o JAR gerado na etapa anterior (confirme se o nome está correto)
COPY --from=build /app/target/FiscalSystemAPI-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
