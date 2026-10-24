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
  Corso di Laurea in Sicurezza Informatica e Tecnologie Cloud<br>
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
    - [2.4 Architettura e Struttura](#24-architettura-e-struttura)
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
| **Jakarta Servlet API** | 5.0.0 | controller HTTP |
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

Il progetto segue il pattern **MVC (model-View-controller)**:

- **model**: i Bean (`Utente`, `Prodotto`, `Carta`, `AcquistoProdotti`) e i DAO (`UtenteDAO`, `ProdottoDAO`, `CartaDAO`, `AcquistoProdottiDAO`)
- **View**: pagine JSP in `/WEB-INF/results/` e `/WEB-INF/amministratore/`
- **controller**: Servlet mappate via `@WebServlet`

**Diagramma delle classi principali (semplificato):**

```text
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

```text
Browser / Client
     │
     │ :8080 (HTTP)
     ▼
Apache Tomcat 10.1
     │
     ├── controller (Servlet)
     ├── model (DAO + Bean)
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

### 2.4 Architettura e Struttura

Il progetto segue un'architettura **MVC (model-View-controller)** con separazione netta tra logica di business, presentazione e accesso ai dati, secondo lo standard **Jakarta EE (Servlet + JSP)**.

#### Struttura delle directory

```text
PharmatexSESCS/src/
├── main/
│   ├── java/
│   │   ├── controller/              # Servlet (controller HTTP)
│   │   │   ├── AggiuntaProdottoServlet.java
│   │   │   ├── CarrelloServlet.java
│   │   │   ├── CercaProdottoPerModificaServlet.java
│   │   │   ├── DatiPagamentoServlet.java
│   │   │   ├── FiltraggioServletMateriale.java
│   │   │   ├── FiltraggioServletPrezzo.java
│   │   │   ├── HomeServlet.java
│   │   │   ├── HomeServletAmministratore.java
│   │   │   ├── InizioServlet.java
│   │   │   ├── LoginServlet.java
│   │   │   ├── MaterialeServlet.java
│   │   │   ├── ModificaProdottiServletAmministratore.java
│   │   │   ├── PagamentoServlet.java
│   │   │   ├── Registrazione.java
│   │   │   ├── RegistrazioneServlet.java
│   │   │   ├── RendiAmministratoreServlet.java
│   │   │   ├── RicercaServlet.java
│   │   │   ├── RimuoviProdottoServlet.java
│   │   │   ├── ServletErrorHelper.java    # Utility: gestione errori HTTP
│   │   │   └── JspHelper.java             # Utility: estrazione dati per JSP
│   │   │
│   │   └── model/                    # Bean + DAO + Utility
│   │       ├── Utente.java           # Bean
│   │       ├── UtenteDAO.java        # DAO
│   │       ├── Prodotto.java         # Bean
│   │       ├── ProdottoDAO.java      # DAO
│   │       ├── Carta.java            # Bean
│   │       ├── CartaDAO.java         # DAO
│   │       ├── AcquistoProdotti.java # Bean
│   │       ├── AcquistoProdottiDAO.java # DAO
│   │       ├── ConPool.java          # Connection pool (fail-fast)
│   │       └── DataAccessException.java # Eccezione custom
│   │
│   └── webapp/                       # View (JSP)
│       ├── immagini/                 # Immagini prodotti e UI
│       ├── ParteCSS/                 # Fogli di stile
│       ├── ParteHTML/                # Frammenti comuni
│       │   ├── navBar.jsp
│       │   ├── navBarAmministratore.jsp
│       │   └── Filter.html
│       ├── WEB-INF/                  # Protetto da accesso diretto
│       │   ├── results/              # JSP di output
│       │   └── amministratore/       # JSP solo per admin
│
└── test/
    └── java/
        └── security/                 # Suite di test di sicurezza
            ├── unit/                 # Test unitari + analisi statica
            │   ├── authorization/
            │   ├── businesslogic/
            │   ├── daointegration/   # Analisi statica del sorgente
            │   ├── dataprotection/
            │   └── inputvalidation/
            │
            └── functional/           # Test funzionali H2
                ├── BaseH2Test.java       # Configurazione H2
                ├── BaseServletH2Test.java # Mock Servlet API
                ├── TestFunctions.java    # SHA1 in Java per alias H2
                ├── authorization/
                ├── businesslogic/
                ├── daointegration/
                ├── exceptions/
                └── inputvalidation/
```

#### Ruolo delle directory

**`Controller/` — Servlet**

Ogni Servlet gestisce un **endpoint HTTP specifico** e ha una singola responsabilità. Il nome è autoesplicativo e segue una convenzione verbale:

| Categoria | Servlet | Responsabilità |
|:----------|:--------|:---------------|
| **Autenticazione** | `LoginServlet`, `RegistrazioneServlet`, `Registrazione` | Gestione ciclo di vita sessione utente |
| **Catalogo (pubblico)** | `HomeServlet`, `InizioServlet`, `RicercaServlet`, `MaterialeServlet`, `FiltraggioServletMateriale`, `FiltraggioServletPrezzo` | Visualizzazione prodotti, ricerca, filtri |
| **Carrello** | `CarrelloServlet` | Gestione carrello (sessione + DB) |
| **Ordini e pagamento** | `PagamentoServlet`, `DatiPagamentoServlet` | Checkout, storico ordini, dettagli |
| **Admin — prodotti** | `AggiuntaProdottoServlet`, `ModificaProdottiServletAmministratore`, `CercaProdottoPerModificaServlet`, `RimuoviProdottoServlet` | CRUD prodotti (solo admin) |
| **Admin — utenti** | `HomeServletAmministratore`, `RendiAmministratoreServlet` | Dashboard admin, promozione utenti |
| **Utility** | `ServletErrorHelper`, `JspHelper` | Gestione errori HTTP centralizzata, estrazione dati per JSP |

Ogni Servlet delega l'accesso ai dati al **DAO** corrispondente e la presentazione alla **JSP**. Non contiene query SQL direttamente.

**`Model/` — Bean + DAO + Utility**

Il model è suddiviso in **tre tipi di classi**:

| Tipo | Classi | Ruolo |
|:---|:---|:---|
| **Bean** (4) | `Utente`, `Prodotto`, `Carta`, `AcquistoProdotti` | Contenitori di dati (POJO) con getter/setter e logica minima (es. hashing password in `Utente`) |
| **DAO** (4) | `UtenteDAO`, `ProdottoDAO`, `CartaDAO`, `AcquistoProdottiDAO` | Accesso al database tramite `PreparedStatement` (no SQL Injection) e try-with-resources (no resource leak) |
| **Utility** (2) | `ConPool`, `DataAccessException` | Connection pool Tomcat con **fail-fast**: se `MYSQL_PASSWORD` non è impostata, l'app non parte. Eccezione custom per semantica degli errori |

**Caratteristiche del model:**

- **Bean serializzabili**: tutti i bean implementano `Serializable` (requisito Tomcat per la persistenza di sessione)
- **Tutti i DAO usano `PreparedStatement`**: nessuna concatenazione di stringhe SQL (protezione da SQL Injection)
- **Tutti i DAO usano try-with-resources**: chiusura automatica di `Connection`, `PreparedStatement`, `ResultSet`
- **`ConPool` singleton static**: pattern Singleton per la `DataSource`

**`webapp/` — View (JSP)**

La directory segue la separazione tra **pagine pubbliche** e **pagine protette**:

| Path | Visibilità | Esempi |
|:---|:---|:---|
| `webapp/*.jsp` | Pubblico (accessibile direttamente) | `HomePage.jsp`, `Login.jsp`, `RegisterUser.jsp` |
| `webapp/WEB-INF/` | **Protetto** (solo forward interno dal Servlet) | JSP di output, admin panel, pagine di errore |
| `webapp/immagini/` | Pubblico | Immagini prodotti, logo, UI |
| `webapp/ParteCSS/` | Pubblico | Fogli di stile |
| `webapp/ParteHTML/` | **Protetto** (incluso tramite `<%@ include %>`) | Frammenti comuni (navBar, Filter) |

**Nota sulla sicurezza:** le JSP dentro `WEB-INF/` **non sono accessibili direttamente dal browser**. Solo i Servlet possono farci forward tramite `RequestDispatcher`. Questo previene accessi non autorizzati a pagine che assumono uno stato di sessione valido (es. pagine admin, dettagli ordine).

**`test/java/security/` — Suite di test**

La suite è organizzata specularmente al codice di produzione, con **due strategie complementari**:

| Directory | Tipo | Approccio |
|:---|:---|:---|
| **`unit/`** | **Test unitari + analisi statica** | Mockito per le Servlet; lettura del sorgente con `Files.readAllBytes` e verifica di pattern (`source.contains(...)`) per i DAO |
| **`functional/`** | **Test funzionali H2** | Esegue i Servlet e i DAO con Mockito (Servlet API) e H2 in-memory (database reale) |

Entrambe le directory sono ulteriormente suddivise per **area OWASP** (`authorization`, `businesslogic`, `daointegration`, `dataprotection`, `inputvalidation`), così è immediato capire quale area di sicurezza copre ogni test.

**Classi di supporto:**

- **`BaseH2Test`** — Classe astratta che configura H2 in-memory con lo schema completo, registra l'alias `SHA1()` per compatibilità con MySQL, e fornisce i metodi `cleanDatabase()` e `executeSql()`.
- **`BaseServletH2Test`** — Estende `BaseH2Test` e fornisce mock preconfigurati di `HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `RequestDispatcher`, oltre ai metodi helper `invokeDoGet()`, `invokeDoPost()`, `createAdminUser()`, `createNormalUser()`.

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

```text
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
| 3 | 03-22/10 | **Test Suite** | Scrittura dei test OWASP + fix vulnerabilità logiche | **494 test** + 18 vulnerabilità risolte |
| 4 | 05-23/10 | **SonarCloud** | Analisi qualità + risoluzione issue | Quality Gate PASSED, **~270 issue risolte** |
| 5 | 09/10 | **Pipeline CI** | Hub workflow + orchestrazione | Pipeline DevSecOps completa |

**Nota metodologica:** l'ordine sequenziale non è casuale. GitGuardian è stato eseguito per primo perché un segreto committato nella history Git è la vulnerabilità più critica (accesso diretto a credenziali reali). Una volta garantita l'assenza di segreti, si è passati alle dipendenze (Snyk) e poi ai test e alla qualità (SonarCloud).

---

## 5. Sicurezza

Il progetto integra **3 tool di sicurezza automatici** e una **suite di 494 test** manuali. Ogni strumento copre un dominio diverso e complementare. I report dettagliati sono nei documenti dedicati.

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
| CVE sul base image Docker | 103 | **112** (accettate: non fixabili) |
| Finding SAST (Snyk Code) | 37 | **0** |

**Vulnerabilità risolte:** 5 CVE High/Medium aggiornando `mysql-connector-java` da 8.0.18 a `com.mysql:mysql-connector-j:9.3.0` (cambio coordinate Maven).

**Finding accettati:** 112 CVE Medium/Low sul base image `tomcat:10.1-jre17-temurin-jammy` (Snyk conferma che è la versione più sicura disponibile) + falsi positivi su SHA-1 e test.

> 📄 **Report dettagliato:** [`docs/Snyk/README.md`](docs/Snyk/README.md)

### 5.3 Test di Sicurezza

**Suite:** 494 test su 51 classi, organizzati in 5 aree OWASP.

| Categoria | Approccio | Classi | Test |
|-----------|-----------|:------:|:----:|
| **Test Unitari + Analisi Statica** | Mockito + lettura sorgente | 30 | 319 |
| **Test Funzionali H2** | Esecuzione contro H2 in-memory | 21 | 175 |
| **TOTALE** | | **51** | **494** |

**Copertura per area OWASP:**

| Area OWASP | Test | Vulnerabilità risolte |
|:---|:---:|:---:|
| **A01 — Broken Access Control** | 129 | 7 |
| **A02 — Cryptographic Failures** | 20 | 0 (rischio accettato) |
| **A03 — Injection** | 246 | 2 |
| **A04 — Insecure Design** | 97 | 3 |
| **DAO Security** | 86 | 1 |

**Risultati:**
- **18 vulnerabilità risolte** — principalmente CWE-862 (Missing Authorization), CWE-20 (Input Validation), CWE-79 (XSS), CWE-178 (Case Sensitivity), CWE-916.
- **~14 finding documentati** come rischio accettato.
- **Il valore aggiunto dei test H2:** ha scoperto SEC-DAO-01 (ricerca case-insensitive), SEC-IV-03 e FUNC-IV-03, invisibili all'analisi statica e a SonarQube.

> 📄 **Report dettagliati:**
> - **Test unitari e analisi statica:** [`docs/TestCase/unit.md`](docs/TestCase/unit.md)
> - **Test funzionali H2:** [`docs/TestCase/functional.md`](docs/TestCase/functional.md)
> - **Hub panoramica:** [`docs/TestCase/security-tests.md`](docs/TestCase/security-tests.md)

### 5.4 SonarCloud — Qualità e Coverage

**Tool:** SonarCloud (SonarSource)  
**Tipi di analisi:** SAST + Quality + Coverage (JaCoCo)

**Quality Gate "Sonar way":** valuta **solo il New Code** (ultimi 7 giorni).

**Baseline iniziale:** Quality Gate **FAILED** con 46 issue — Security Rating B.

**Risultato finale (New Code):**

| Metrica | Valore | Soglia |
|:---|:---:|:---:|
| Quality Gate | **PASSED** | — |
| New Issues | **0** | 0 |
| Coverage | **83.01%** | ≥ 80% |
| Duplications | **1.55%** | ≤ 3% |
| Security Rating | **A** | A |
| Reliability Rating | **A** | A |
| Maintainability Rating | **A** | A |

**Risultato Overall Code (dopo remediation):**

| Categoria | Prima | Dopo | Δ | Rating |
|-----------|:-----:|:----:|:---:|:------:|
| Security | 93 | **0** | **-93** | D → **A** |
| Reliability | 103 | **0** | **-103** | **E → A** |
| Maintainability | 74 | **0** | **-74** | A → A |
| **Totale** | **270** | **0** | **-270** | — |

**Interventi principali:** azzeramento totale delle issue su file `.java`, `.jsp`, `.html` e `.css`.

> 📄 **Report dettagliato:** [`docs/SonarCloud/README.md`](docs/SonarCloud/README.md)

---

## 6. Pipeline CI/CD

La pipeline è orchestrata da un **workflow hub** (`ci.yml`) che chiama 4 workflow figli in sequenza.

### 6.1 Ordine di Esecuzione

```text
git push
   │
   ▼
1. Secret Scanning (GitGuardian)  ─── se trova segreti → STOP
   │
   ▼
2. Test Suite (494 test)         ─── se falliscono → STOP
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
| **Segreti** | GitHub Secrets (vedi tabella sotto) |
| **Docker Hub** | Login con Access Token (non password) |

**Secrets richiesti:**

| Secret | Servizio utilizzato | Workflow | Tipo | Utilizzo |
|:---|:---|:---|:---|:---|
| `SONAR_TOKEN` | SonarCloud | Analisi statica del codice | Token API | Autenticazione per l'analisi della qualità e della sicurezza del codice |
| `SNYK_API_KEY` | Snyk | Security scan delle dipendenze | API Key | Rilevamento di vulnerabilità nelle dipendenze del progetto |
| `GITGUARDIAN_API_KEY` | GitGuardian | Secret scanning | API Key | Rilevamento di credenziali e segreti esposti nel repository |
| `DOCKERHUB_USERNAME` | Docker Hub | Build e pubblicazione dell'immagine | Username | Identificazione dell'account utilizzato per pubblicare l'immagine Docker |
| `DOCKERHUB_TOKEN` | Docker Hub | Build e pubblicazione dell'immagine | Access Token | Autenticazione per il push dell'immagine Docker sul registry |

---

## 7. Containerizzazione

### 7.1 Dockerfile

Il `Dockerfile` è di tipo **multi-stage** per ridurre la dimensione dell'immagine finale:

```dockerfile
# Stage 1: Builder (Maven + JDK)
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Aggiorna i pacchetti di sistema per ridurre le vulnerabilità (Snyk)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Tomcat + JRE)
FROM tomcat:10.1-jre17-temurin-jammy

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
```

**Scelte di sicurezza:**

| Scelta | Motivazione |
|--------|-------------|
| **Multi-stage build** | JDK e Maven restano nello stage builder, non nell'immagine finale |
| **JRE invece di JDK** | Riduce la superficie d'attacco (-15 CVE Medium) |
| **`apt-get upgrade` (Builder)** | Applica le patch di sicurezza nello stage di build. Sebbene questo stage venga scartato, garantisce che eventuali tool utilizzati durante la compilazione non introducano vulnerabilità. |
| **`apt-get upgrade` (Runtime)** | Applica le patch di sicurezza sull'immagine finale (Ubuntu 22.04) che verrà pubblicata e scansionata da Snyk. |
| **Nessun segreto nell'immagine** | Credenziali lette da variabili d'ambiente a runtime ||

### 7.2 Docker Compose

Il file `docker-compose.yml` orchestra 2 container: l'applicazione (Tomcat) e il database (MySQL).

```yaml
services:
  app:
    image: domfalco/pharmatex:latest
    container_name: pharmatex-app
    restart: unless-stopped
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
    container_name: mysql-pharmatex
    restart: unless-stopped
    # FIX ENCODING: Forza MySQL a usare UTF-8
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
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

**Configurazioni chiave del Docker Compose:**

| Configurazione | Motivazione |
|--------|-------------|
| **`container_name`** | Assegna nomi fissi e leggibili ai container (`pharmatex-app` e `mysql-pharmatex`), facilitando i comandi di monitoraggio e debug. |
| **`restart: unless-stopped`** | Garantisce l'alta disponibilità: i container si riavviano automaticamente in caso di crash o riavvio della macchina host. |
| **`command` (Encoding)** | Forza MySQL a utilizzare il set di caratteri `utf8mb4` e la collation `utf8mb4_unicode_ci`, prevenendo problemi di encoding con caratteri speciali e accenti. |
| **`healthcheck`** | Assicura che il container dell'applicazione (Tomcat) parta solo dopo che il database MySQL è completamente pronto ad accettare connessioni. |
| **`volumes`** | Utilizza un volume nominato (`db_data`) per la persistenza dei dati e monta `Pharmatex.sql` per l'inizializzazione automatica del database al primo avvio. |

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
| **A02 — Cryptographic Failures** | Risolto | 20 | SHA-1 sostituito con BCrypt |
| **A03 — Injection** | Risolto | 246 | SQL Injection neutralizzata (PreparedStatement) |
| **A04 — Insecure Design** | Risolto | 97 | 3 vulnerabilità CWE-862 risolte |
| **A05 — Security Misconfiguration** | Risolto | — | Configurazione sicura Docker |
| **A07 — Authentication Failures** | Risolto | — | Sessioni e autorizzazione |
| **A09 — Logging & Monitoring** | Risolto | — | Logger al posto di `System.err` |

---

## 9. Riepilogo Finale

| Metrica | Valore |
|---------|:------:|
| **Test totali** | **494** (319 unitari + 175 funzionali H2) |
| **Classi di test** | **51** |
| **Aree OWASP coperte** | **5** |
| **Vulnerabilità risolte** | **22** (1 GitGuardian + 5 Snyk + 16 Test) |
| **Finding documentati (rischio accettato)** | **~14** |
| **Issue SonarCloud risolte** | **~109 (New Code) + 270 (Overall Code)** |
| **Quality Gate SonarCloud** | **PASSED** |
| **Coverage New Code** | **83.01%** |
| **Duplicazioni New Code** | **1.55%** |
| **Reliability Rating Overall** | **E → A** |
| **Immagine Docker Hub** | Pubblica |
| **Pipeline CI/CD** | Completa (5 workflow) |

### Flusso Complessivo

```text
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
│  FASE 3 — Test Suite (03-22/10)                                  │
│  Suite di 494 test OWASP                                         │
│  → 18 vulnerabilità risolte                                      │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 4 — SonarCloud (05-23/10)                                  │
│  Quality + Coverage + SAST                                       │
│  → Quality Gate PASSED, Reliability E → A                        │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│  FASE 5 — Pipeline CI/CD (09/10)                                 │
│  Hub orchestrato + Docker Publish                                │
│  → Pipeline DevSecOps completa                                   │
└──────────────────────────────────────────────────────────────────┘
```
