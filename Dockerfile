# Usa Maven per costruire il WAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Aggiorna i pacchetti di sistema per ridurre le vulnerabilità (Snyk)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Tomcat aggiornato e uso di JRE per ridurre la superficie d'attacco
FROM tomcat:11.0.23-jre17-temurin-alpine

# Aggiorna i pacchetti di sistema per ridurre le vulnerabilità (Snyk)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

# Rimuove le app predefinite di Tomcat (opzionale)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia il WAR generato nella cartella webapps di Tomcat
COPY --from=builder /app/target/Progetto-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Espone la porta per accedere all'app (default Tomcat)
EXPOSE 8080

# Imposta variabili per la connessione al DB
ENV MYSQL_HOST=db
ENV MYSQL_PORT=3306

# Avvia Tomcat
CMD ["catalina.sh","run"]