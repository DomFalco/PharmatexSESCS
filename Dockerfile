# Usa Maven per costruire il WAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Usa Tomcat per eseguire l'applicazione
FROM tomcat:10.1-jdk17

# Rimuove le app predefinite di Tomcat (opzionale)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia il WAR generato nella cartella webapps di Tomcat
COPY --from=builder /app/target/Progetto-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Espone la porta per accedere all'app (default Tomcat)
EXPOSE 8080

# Avvia Tomcat
CMD ["catalina.sh","run"]