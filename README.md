# PharmatexSESCS

<p align="center">
  <a href="https://www.unisa.it">
    <img src="https://www.unisa.it/rescue/img/logo_standard.png" alt="Unisa" width="180">
  </a>
</p>

<h1 align="center">Pharmatex</h1>
<p align="center"><em>E-commerce per la vendita di prodotti per il riposo</em></p>

<p align="center">
  <strong>Università degli Studi di Salerno</strong><br>
  Corso di Laurea in Sicurezza Informatica per Tecnologie Cloud<br>
  Software Engineering for Secure Cloud Systems · A.A. 2025/2026
</p>

<p align="center">
  <a href="https://github.com/DomFalco/PharmatexSESCS/actions/workflows/ci.yml">
    <img src="https://github.com/DomFalco/PharmatexSESCS/actions/workflows/ci.yml/badge.svg?branch=master" alt="CI Pipeline">
  </a>
  <a href="https://hub.docker.com/r/domfalco/pharmatex">
    <img src="https://img.shields.io/badge/Docker%20Hub-domfalco%2Fpharmatex-2496ED?logo=docker&logoColor=white" alt="Docker Hub">
  </a>
  <a href="https://sonarcloud.io/project/overview?id=DomFalco_PharmatexSESCS">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=DomFalco_PharmatexSESCS&metric=alert_status" alt="Quality Gate">
  </a>
  <a href="https://app.snyk.io">
    <img src="https://img.shields.io/badge/Snyk-0%20High%20Vulnerabilities-4C4A73?logo=snyk&logoColor=white" alt="Snyk">
  </a>
  <a href="https://dashboard.gitguardian.com">
    <img src="https://img.shields.io/badge/GitGuardian-0%20Secrets%20Leaked-FF6B6B?logo=gitguardian&logoColor=white" alt="GitGuardian">
  </a>
</p>

| Risorsa | Link |
|---------|------|
| **Docker Hub** | [hub.docker.com/r/domfalco/pharmatex](https://hub.docker.com/r/domfalco/pharmatex) |
| **Dashboard SonarCloud** | [sonarcloud.io/project/overview?id=DomFalco_PharmatexSESCS](https://sonarcloud.io/project/overview?id=DomFalco_PharmatexSESCS) |

<table align="center">

  <tr>
    <th>Studente</th>
    <th>Matricola</th>
  </tr>
  <tr>
    <td>Falco Domenico</td>
    <td>0522700023</td>
  </tr>

</table>

---

## Indice

1. [Descrizione dell'Applicazione, Stack Tecnologico e System Design](#1-descrizione-dellapplicazione-stack-tecnologico-e-system-design)
    - [1.1 Descrizione dell'Applicazione](#11-descrizione-dellapplicazione)
    - [1.2 Stack Tecnologico](#12-stack-tecnologico)
    - [1.3 System Design — Class Diagram](#13-system-design--class-diagram)
2. [Architettura e Ruoli](#2-architettura-e-ruoli)
    - [2.1 Architettura](#21-architettura)
    - [2.2 Ruoli](#22-ruoli)
    - [2.3 Matrice di Accesso RBAC](#23-matrice-di-accesso-rbac)
3. [API Reference](#3-api-reference)
4. [Metodologia e Flusso di Lavoro](#4-metodologia-e-flusso-di-lavoro)
5. [Sicurezza](#5-sicurezza)
    - [5.1 GitGuardian — Secret Scanning](#51-gitguardian--secret-scanning)
    - [5.2 Snyk — SCA + SAST + Container](#52-snyk--sca--sast--container)
    - [5.3 Test di Sicurezza](#53-test-di-sicurezza)
    - [5.4 SonarCloud — Qualità e Coverage](#54-sonarcloud--qualità-e-coverage)
6. [Pipeline CI/CD](#6-pipeline-cicd)
    - [6.1 Ordine di Esecuzione](#61-ordine-di-esecuzione)
    - [6.2 Vantaggi](#62-vantaggi)
    - [6.3 Sicurezza della Pipeline](#63-sicurezza-della-pipeline)
7. [Containerizzazione](#7-containerizzazione)
    - [7.1 Dockerfile](#71-dockerfile)
    - [7.2 Docker Compose](#72-docker-compose)
    - [7.3 Immagine su Docker Hub](#73-immagine-su-docker-hub)
8. [Conformità OWASP Top 10](#8-conformità-owasp-top-10)
9. [Riepilogo Finale](#9-riepilogo-finale)

---

## 1. Descrizione dell'Applicazione, Stack Tecnologico e System Design

### 1.1 Descrizione dell'Applicazione

**PharmatexSESCS** è un'applicazione web e-commerce per la vendita di prodotti per il riposo (materassi, reti, letti, divani, cuscini). La piattaforma copre l'intero ciclo di acquisto: navigazione del catalogo, aggiunta al carrello, autenticazione, pagamento e riepilogo ordini.

**Funzionalità principali:**

| Area | Funzionalità |
|------|-------------|
| **Catalogo** | Navigazione per categoria (materassi, reti, letti, divani, cuscini) con filtri per materiale e prezzo |
| **Ricerca** | Ricerca per nome prodotto con query case-insensitive |
| **Carrello** | Aggiunta, modifica quantità, rimozione articoli |
| **Autenticazione** | Login/logout con hashing delle password |
| **Registrazione** | Creazione nuovo account con validazione |
| **Pagamento** | Inserimento dati carta di credito e checkout |
| **Riepilogo Ordini** | Storico acquisti per utente autenticato |
| **Area Admin** | Gestione prodotti, visualizzazione utenti e ordini |

### 1.2 Stack Tecnologico

| Tecnologia | Versione | Scopo |
|------------|:--------:|-------|
| **Java** | 8 | Linguaggio principale |
| **Jakarta Servlet API** | 5.0.0 | Controller HTTP |
| **JSP (Jakarta Server Pages)** | 3.0 | View layer |
| **MySQL Connector/J** | 9.3.0 | Driver JDBC (aggiornato per CVE Snyk) |
| **Apache Tomcat** | 10.1 | Servlet Container |
| **Maven** | 3.9.6 | Build system |
| **H2 Database** | 2.2.224 | DB in-memory per test funzionali |
| **JUnit Jupiter** | 5.8.1 | Framework di test |
| **Mockito** | 4.11.0 | Mock Servlet API |
| **AssertJ** | 3.27.7 | Asserzioni fluent |
| **Jsoup** | 1.23.2 | Parsing HTML nei test |

### 1.3 System Design — Class Diagram

Il progetto segue il pattern **MVC (Model-View-Controller)**:

- **Model**: i Bean (`Utente`, `Prodotto`, `Carta`, `AcquistoProdotti`) e i DAO (`UtenteDAO`, `ProdottoDAO`, `CartaDAO`, `AcquistoProdottiDAO`)
- **View**: pagine JSP in `/WEB-INF/results/` e `/WEB-INF/amministratore/`
- **Controller**: Servlet mappate via `@WebServlet`

**Diagramma delle classi principali (semplificato):**

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│    Utente       │      │    Prodotto     │      │     Carta       │
├─────────────────┤      ├─────────────────┤      ├─────────────────┤
│ - email         │      │ - idProdotto    │      │ - numeroCarta   │
│ - password      │      │ - nomeProd      │      │ - nomeIntestario│
│ - nome          │      │ - prezzo        │      │ - dataScadenza  │
│ - cognome       │      │ - quantita      │      │ - CVV           │
│ - amministratore│      │ - nomeCategoria │      │ - u: Utente     │
└────────┬────────┘      └────────┬────────┘      └────────┬────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   UtenteDAO     │      │  ProdottoDAO    │      │    CartaDAO     │
└────────┬────────┘      └────────┬────────┘      └────────┬────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  ▼
                         ┌─────────────────┐
                         │    ConPool      │
                         └────────┬────────┘
                                  ▼
                         ┌─────────────────┐
                         │     MySQL 8     │
                         └─────────────────┘
```

**Pattern architetturali adottati:**

| Pattern | Dove | Motivazione |
|---------|------|-------------|
| **DAO Pattern** | Tutti i DAO | Separazione tra accesso ai dati e logica di business |
| **Custom Exception** | `DataAccessException` | Eccezione dedicata al posto di `RuntimeException` generica |
| **Fail-fast** | `ConPool` | `IllegalStateException` se `MYSQL_PASSWORD` non è impostata |
| **Central Error Handling** | `ServletErrorHelper` | Evita duplicazione di `sendError` in 9 Servlet |
| **Try-with-resources** | Tutti i DAO | Chiusura automatica di `Connection`, `PreparedStatement`, `ResultSet` |

---

## 2. Architettura e Ruoli

### 2.1 Architettura

L'architettura è **a tre livelli**:

1. **Presentation Layer** (JSP + Servlet): gestisce la UI e le richieste HTTP
2. **Business Logic Layer** (Bean + DAO): incapsula la logica di business e l'accesso ai dati
3. **Data Layer** (MySQL 8): persistenza dei dati

```
Browser / Client
     │
     │ :8080 (HTTP)
     ▼
Apache Tomcat 10.1
     │
     ├── Controller (Servlet)
     ├── Model (DAO + Bean)
     └── View (JSP)
     │
     ▼
MySQL 8
```

### 2.2 Ruoli

| Ruolo | Descrizione |
|:------|:------------|
| **Anonimo** | Può visualizzare catalogo, ricerca, dettagli prodotto. Non può accedere al carrello né al checkout |
| **Utente** | Può aggiungere/rimuovere prodotti dal carrello, effettuare ordini, gestire il proprio profilo |
| **Admin** | Può gestire prodotti (CRUD), visualizzare utenti e ordini, promuovere/declassare altri utenti |

### 2.3 Matrice di Accesso RBAC

| Endpoint / Risorsa | Anonimo | Utente | Admin |
|:---|:---:|:---:|:---:|
| Catalogo, Ricerca, Dettaglio prodotto | ✅ | ✅ | ✅ |
| Carrello (`CarrelloServlet`) | ❌ | ✅ | ✅ |
| Checkout (`PagamentoServlet`, `DatiPagamentoServlet`) | ❌ | ✅ | ✅ |
| Riepilogo ordini utente | ❌ | ✅ | ✅ |
| Gestione prodotti (CRUD) | ❌ | ❌ | ✅ |
| Ricerca prodotto admin | ❌ | ❌ | ✅ |
| Gestione utenti | ❌ | ❌ | ✅ |
| Dashboard admin | ❌ | ❌ | ✅ |

---

## 3. API Reference

Elenco delle Servlet principali con metodo HTTP, endpoint, descrizione e livello di autenticazione richiesto.

| Metodo | Endpoint | Descrizione | Auth |
|:------:|:---------|:------------|:----:|
| GET | `/HomePage` | Homepage con 5 prodotti casuali | Pubblico |
| GET | `/InizioServlet?action=...` | Instrada verso le pagine di categoria | Pubblico |
| GET | `/RicercaServlet?search=...` | Ricerca testuale case-insensitive | Pubblico |
| GET | `/MaterialeServlet?prodotto=...` | Filtra i prodotti per materiale | Pubblico |
| GET | `/FiltraggioServletMateriale?prodotto=...&materiale=...` | Filtro materiale dinamico | Pubblico |
| GET | `/FiltraggioServletPrezzo?prezzomin=...&prezzomax=...` | Filtro prezzo | Pubblico |
| POST | `/LoginServlet` | Login utente | Pubblico |
| GET | `/LoginServlet?action=logout` | Logout e invalidazione sessione | Utente |
| GET | `/LoginServlet?action=carrello` | Visualizza carrello | Utente |
| GET | `/LoginServlet?action=riepilogo` | Riepilogo acquisti utente | Utente |
| POST | `/RegistrazioneServlet` | Registrazione nuovo utente | Pubblico |
| GET | `/CarrelloServlet?action=...&quantita=...` | Aggiungi/rimuovi prodotto dal carrello | Utente |
| POST | `/PagamentoServlet` | Checkout e pagamento (simulato) | Utente |
| POST | `/DatiPagamentoServlet` | Inserimento dati carta | Utente |
| GET | `/HomeServletAmministratore` | Dashboard amministratore | Admin |
| GET/POST | `/AggiuntaProdottoServlet` | Aggiungi nuovo prodotto | Admin |
| GET/POST | `/CercaProdottoPerModificaServlet?search=...` | Cerca prodotto da modificare | Admin |
| GET/POST | `/ModificaProdottiServletAmministratore` | Modifica prezzo/quantità prodotto | Admin |
| GET | `/RimuoviProdottoServlet` | Rimuovi prodotto dal catalogo | Admin |
| GET | `/RendiAmministratoreServlet?action=...` | Promuovi/declassa utente ad admin | Admin |

---

## 4. Metodologia e Flusso di Lavoro

Il progetto ha seguito un **approccio incrementale guidato dagli strumenti di sicurezza**, procedendo per fasi sequenziali. Ogni strumento ha rivelato vulnerabilità specifiche del proprio dominio, che sono state risolte prima di passare allo step successivo.

```
FASE 1: GitGuardian       → Segreti hardcoded nel codice
FASE 2: Snyk              → Vulnerabilità in dipendenze, container e codice
FASE 3: Test di sicurezza → Identificazione di vulnerabilità logiche
FASE 4: SonarCloud        → Qualità del codice e coverage
FASE 5: Pipeline CI/CD    → Orchestrazione automatizzata
```

| Fase | Data | Strumento | Azione | Risultato |
|:----:|:----:|-----------|--------|-----------|
| 1 | 28/09 | **GitGuardian** | Scansione iniziale + fix credenziali hardcoded | 1 vulnerabilità critica risolta |
| 2 | 01/10 | **Snyk** | Scansione dipendenze Maven + Dockerfile + SAST | 5 vulnerabilità risolte |
| 3 | 03-07/10 | **Test Suite** | Scrittura dei test OWASP + fix vulnerabilità logiche | 481 test + 14 vulnerabilità risolte |
| 4 | 05-07/10 | **SonarCloud** | Analisi qualità + risoluzione issue | Quality Gate PASSED, ~80 issue risolte |
| 5 | 09/10 | **Pipeline CI** | Hub workflow + orchestrazione | Pipeline DevSecOps completa |

**Nota metodologica:** l'ordine sequenziale non è casuale. GitGuardian è stato eseguito per primo perché un segreto committato nella history Git è la vulnerabilità più critica (accesso diretto a credenziali reali). Una volta garantita l'assenza di segreti, si è passati alle dipendenze (Snyk) e poi ai test e alla qualità (SonarCloud).

---

## 5. Sicurezza

Il progetto integra **3 tool di sicurezza automatici** e una **suite di 481 test** manuali. Ogni strumento copre un dominio diverso e complementare. I report dettagliati sono nei documenti dedicati.

### 5.1 GitGuardian — Secret Scanning

**Tool:** GitGuardian (ggshield)  
**Tipo:** Secret Scanning (rilevamento credenziali hardcoded)  
**Trigger:** ad ogni push su `main`/`master` e PR

**Risultato:** 1 vulnerabilità critica risolta — **SEC-01: Credenziale DB Hardcoded** in `ConPool.java` (CWE-798, CVSS 9.8).

**Fix applicato:** lettura da variabili d'ambiente (`System.getenv()`) + fail-fast `IllegalStateException` se `MYSQL_PASSWORD` non è impostata. Rotazione della credenziale compromessa.

**Stato attuale:** `No secrets have been found` ad ogni push.

> 📄 **Report dettagliato:** [`docs/GitGuardian/README.md`](docs/GitGuardian/README.md)

### 5.2 Snyk — SCA + SAST + Container

**Tool:** Snyk  
**Tipi di analisi:**
- **SCA**: dipendenze Maven (`pom.xml`)
- **SAST**: codice sorgente Java (Snyk Code)
- **Container**: immagine Docker (base image)

**Configurazione soglia:** `--severity-threshold=high` — solo issue HIGH/CRITICAL bloccano la pipeline

**Risultati principali:**

| Categoria | Prima | Dopo |
|:---|:---:|:---:|
| Vulnerabilità dipendenze Maven | 5 | **0** |
| CVE sul base image Docker | 103 | **89** (accettate: non fixabili) |
| Finding SAST (Snyk Code) | 31 LOW | accettate (falsi positivi su test) |

**Vulnerabilità risolte:** 5 CVE High/Medium aggiornando `mysql-connector-java` da 8.0.18 a `com.mysql:mysql-connector-j:9.3.0` (cambio coordinate Maven).

**Finding accettati:** 89 CVE Medium/Low sul base image `tomcat:10.1-jre17-temurin-jammy` (Snyk conferma che è la versione più sicura disponibile) + 2 SHA-1 weak hash + 29 falsi positivi "Hardcoded Passwords" nei test.

> 📄 **Report dettagliato:** [`docs/Snyk/README.md`](docs/Snyk/README.md)

### 5.3 Test di Sicurezza

**Suite:** 481 test su 50 classi, organizzati in 5 aree OWASP.

| Categoria | Approccio | Classi | Test |
|-----------|-----------|:------:|:----:|
| **Test Unitari + Analisi Statica** | Mockito + lettura sorgente | 29 | 305 |
| **Test Funzionali H2** | Esecuzione contro H2 in-memory | 21 | 176 |
| **TOTALE** | | **50** | **481** |

**Copertura per area OWASP:**

| Area OWASP | Test | Vulnerabilità risolte |
|:---|:---:|:---:|
| **A01 — Broken Access Control** | 129 | 7 |
| **A02 — Cryptographic Failures** | 20 | 0 (rischio accettato) |
| **A03 — Injection** | 233 | 2 |
| **A04 — Insecure Design** | 97 | 3 |
| **DAO Security** | 86 | 1 |

**Risultati:**
- **14 vulnerabilità risolte** — principalmente CWE-862 (Missing Authorization), CWE-20 (Input Validation), CWE-79 (XSS), CWE-178 (Case Sensitivity)
- **~15 finding documentati** come rischio accettato (SHA-1, CSRF, brute-force, session fixation, PCI DSS)
- **Il valore aggiunto dei test H2:** ha scoperto SEC-DAO-01 (ricerca case-insensitive), invisibile all'analisi statica e a SonarQube

> 📄 **Report dettagliati:**
> - **Test unitari e analisi statica:** [`docs/TestCase/unit.md`](docs/TestCase/unit.md)
> - **Test funzionali H2:** [`docs/TestCase/functional.md`](docs/TestCase/functional.md)
> - **Hub panoramica:** [`docs/TestCase/security-tests.md`](docs/TestCase/security-tests.md)

### 5.4 SonarCloud — Qualità e Coverage

**Tool:** SonarCloud (SonarSource)  
**Tipi di analisi:** SAST + Quality + Coverage (JaCoCo)

**Quality Gate "Sonar way":** valuta **solo il New Code** (ultimi 7 giorni).

**Baseline iniziale (05/10/2026):** Quality Gate **FAILED** con 46 issue — Security Rating B.

**Risultato finale (New Code):**

| Metrica | Valore | Soglia |
|:---|:---:|:---:|
| Quality Gate | **PASSED** | — |
| New Issues | **0** | 0 |
| Coverage | **84.76%** | ≥ 80% |
| Duplications | **0.64%** | ≤ 3% |
| Security Rating | **A** | A |
| Reliability Rating | **A** | A |
| Maintainability Rating | **A** | A |

**Risultato Overall Code (dopo remediation):**

| Categoria | Prima | Dopo | Δ | Rating |
|-----------|:-----:|:----:|:---:|:------:|
| Security | 93 | 59 | **-34** | D → D |
| Reliability | 103 | 85 | **-18** | **E → C** |
| Maintainability | 74 | 46 | **-28** | A → A |
| **Totale** | **270** | **190** | **-80** | — |

**Interventi principali:**
- **14 Blocker risolte** — try-with-resources su tutti i DAO
- **16 High risolte** — costanti duplicate, cognitive complexity, `Random`, eccezioni generiche
- **15 Medium risolte** — `DataAccessException` custom, rimozione `SELECT *`
- **23 Low risolte** — import inutilizzati, `throws`, `eq()`, join assertions

> 📄 **Report dettagliato:** [`docs/SonarCloud/README.md`](docs/SonarCloud/README.md)

---

## 6. Pipeline CI/CD

La pipeline è orchestrata da un **workflow hub** (`ci.yml`) che chiama 4 workflow figli in sequenza.

### 6.1 Ordine di Esecuzione

```
git push
   │
   ▼
1. Secret Scanning (GitGuardian)  ─── se trova segreti → STOP
   │
   ▼
2. Test Suite (481 test)         ─── se falliscono → STOP
   │
   ├──────────────────┐
   ▼                  ▼
3. SAST (SonarCloud)  4. SCA (Snyk ×3)  ─── se trovano HIGH/CRITICAL → STOP
   │                  │
   └────────┬─────────┘
            ▼
5. Docker Build & Push  ─── solo su master/main o tag v*
```

### 6.2 Vantaggi

| Vantaggio | Descrizione |
|-----------|-------------|
| **Fail-fast** | Ogni job blocca i successivi in caso di errore |
| **Ottimizzazione** | Test eseguiti una volta sola; Sonar riutilizza report via artifact |
| **Parallelismo** | Snyk e Sonar girano in parallelo (dipendono solo da test) |
| **Sicurezza** | Immagine Docker pubblicata solo se tutti i gate sono verdi |

### 6.3 Sicurezza della Pipeline

| Aspetto | Implementazione |
|---------|-----------------|
| **Permessi minimi** | `permissions: contents: read` globale; permessi specifici per job |
| **SHA pinning** | Action di terze parti pin-nate a SHA commit |
| **Segreti** | GitHub Secrets (`SONAR_TOKEN`, `SNYK_API_KEY`, `GITGUARDIAN_API_KEY`, `DOCKERHUB_*`) |
| **Docker Hub** | Login con Access Token (non password) |

---

## 7. Containerizzazione

### 7.1 Dockerfile

Il `Dockerfile` è di tipo **multi-stage** per ridurre la dimensione dell'immagine finale:

```dockerfile
# Stage 1: Builder (Maven + JDK)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Tomcat + JRE)
FROM tomcat:10.1-jre17-temurin-jammy
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/Progetto-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
ENV MYSQL_HOST=db
ENV MYSQL_PORT=3306
CMD ["catalina.sh","run"]
```

**Scelte di sicurezza:**

| Scelta | Motivazione |
|--------|-------------|
| **Multi-stage build** | JDK e Maven restano nello stage builder, non nell'immagine finale |
| **JRE invece di JDK** | Riduce la superficie d'attacco (-15 CVE Medium) |
| **`apt-get upgrade`** | Applica le patch di sicurezza disponibili in Ubuntu 22.04 |
| **Nessun segreto nell'immagine** | Credenziali lette da variabili d'ambiente a runtime |

### 7.2 Docker Compose

Il file `docker-compose.yml` orchestra 2 container: l'applicazione (Tomcat) e il database (MySQL).

```yaml
services:
  app:
    image: domfalco/pharmatex:latest
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      - MYSQL_HOST=db
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=${MYSQL_DATABASE:-Pharmatex}
      - MYSQL_USER=${MYSQL_USER:-root}
      - MYSQL_PASSWORD=${MYSQL_PASSWORD:-Password}
    networks:
      - ecommerce-net

  db:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD:-Password}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-Pharmatex}
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql
      - ./Pharmatex.sql:/docker-entrypoint-initdb.d/Pharmatex.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p${MYSQL_PASSWORD:-Password}"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - ecommerce-net

networks:
  ecommerce-net:
    driver: bridge

volumes:
  db_data:
```

**Come avviare:**

```bash
git clone https://github.com/DomFalco/PharmatexSESCS.git
cd PharmatexSESCS
docker compose up -d
# Apri http://localhost:8080/
```

**Credenziali di test:**

| Ruolo | Email | Password |
|-------|-------|----------|
| Admin | `admin@gmail.com` | `admin001` |
| Cliente | `cliente1@gmail.com` | `Cliente1` |

### 7.3 Immagine su Docker Hub

**Repository:** [hub.docker.com/r/domfalco/pharmatex](https://hub.docker.com/r/domfalco/pharmatex)

**Tag pubblicati:** `latest`, `master`, `sha-<commit>`

**Come scaricare e avviare:**

```bash
# Avvio completo con docker-compose (consigliato)
git clone https://github.com/DomFalco/PharmatexSESCS.git
cd PharmatexSESCS
docker compose up -d
```

---

## 8. Conformità OWASP Top 10

| Categoria OWASP Top 10 (2021) | Stato | Test | Note |
|-------------------------------|:-----:|:----:|------|
| **A01 — Broken Access Control** | Risolto | 129 | 7 vulnerabilità CWE-862 risolte |
| **A02 — Cryptographic Failures** | Rischio accettato | 20 | SHA-1 documentato (CWE-916) |
| **A03 — Injection** | Risolto | 233 | SQL Injection neutralizzata (PreparedStatement) |
| **A04 — Insecure Design** | Risolto | 97 | 3 vulnerabilità CWE-862 risolte |
| **A05 — Security Misconfiguration** | Risolto | — | Configurazione sicura Docker |
| **A07 — Authentication Failures** | Risolto | — | Sessioni e autorizzazione |
| **A09 — Logging & Monitoring** | Risolto | — | Logger al posto di `System.err` |

---

## 9. Riepilogo Finale

| Metrica | Valore |
|---------|:------:|
| **Test totali** | **481** (305 unitari + 176 funzionali H2) |
| **Classi di test** | **50** |
| **Aree OWASP coperte** | **5** |
| **Vulnerabilità risolte** | **20** (1 GitGuardian + 5 Snyk + 14 Test) |
| **Finding documentati (rischio accettato)** | **~15** |
| **Issue SonarCloud risolte** | **~108 (New Code) + ~80 (Overall Code)** |
| **Quality Gate SonarCloud** | **PASSED** |
| **Coverage New Code** | **84.76%** |
| **Duplicazioni New Code** | **0.64%** |
| **Reliability Rating Overall** | **E → C** |
| **Immagine Docker Hub** | Pubblica |
| **Pipeline CI/CD** | Completa (5 workflow) |

### Flusso Complessivo

```
┌──────────────────────────────────────────────────────────────────┐
│  FASE 1 — GitGuardian (28/09)                                    │
│  Secret scanning + fix credenziali hardcoded                     │
│  → 1 vulnerabilità critica risolta                               │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 2 — Snyk (01/10)                                           │
│  SCA + SAST + Container scanning                                 │
│  → 5 vulnerabilità risolte                                       │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 3 — Test Suite (03-07/10)                                  │
│  Suite di 481 test OWASP                                         │
│  → 14 vulnerabilità risolte                                      │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 4 — SonarCloud (05-07/10)                                  │
│  Quality + Coverage + SAST                                       │
│  → Quality Gate PASSED, Reliability E → C                        │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 5 — Pipeline CI/CD (09/10)                                 │
│  Hub orchestrato + Docker Publish                                │
│  → Pipeline DevSecOps completa                                   │
└──────────────────────────────────────────────────────────────────┘
```