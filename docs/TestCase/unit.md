# Test di Sicurezza — Suite OWASP

**Strategia:** [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)

**Framework:** JUnit 5 + Mockito 4.11.0 + AssertJ 3.27.7

**Test totali:** 304 (29 classi, 5 aree OWASP)

**Data ultima esecuzione:** 07/10/2026

---

## Sommario Esecutivo

Questo report documenta la suite di **test unitari e di analisi statica** del progetto, composta da **304 test** distribuiti su **29 classi** e organizzati in **5 aree** secondo l'OWASP Testing Guide. I **test funzionali H2** (176 test) sono documentati separatamente nel [report dedicato](functional.md).

Il lavoro ha portato alla **risoluzione di 15 vulnerabilità** — principalmente [CWE-862](https://cwe.mitre.org/data/definitions/862.html) (Missing Authorization), [CWE-20](https://cwe.mitre.org/data/definitions/20.html) (Improper Input Validation), [CWE-79](https://cwe.mitre.org/data/definitions/79.html) (XSS), [CWE-476](https://cwe.mitre.org/data/definitions/476.html) (NPE) e [CWE-178](https://cwe.mitre.org/data/definitions/178.html) (Improper Handling of Case Sensitivity) — e alla **documentazione di ulteriori ~15 finding** classificati come **rischio accettato**, tra cui [CWE-916](https://cwe.mitre.org/data/definitions/916.html) (SHA-1 per password), [CWE-352](https://cwe.mitre.org/data/definitions/352.html) (CSRF), [CWE-307](https://cwe.mitre.org/data/definitions/307.html) (brute-force), [CWE-384](https://cwe.mitre.org/data/definitions/384.html) (session fixation) e violazioni PCI DSS.

Le aree coperte sono mappate anche sulle categorie dell'**OWASP Top 10 (2021)** per una lettura immediata da parte di auditor e revisori.

---

## Indice

- [1. Introduzione](#1-introduzione)
    - [1.1 Strategia e organizzazione](#11-strategia-e-organizzazione)
    - [1.2 Struttura delle directory dei test](#12-struttura-delle-directory-dei-test)
    - [1.3 Stack di test](#13-stack-di-test)
- [2. Pattern di test utilizzati](#2-pattern-di-test-utilizzati)
    - [2.1 Test dei Servlet (Mockito)](#21-test-dei-servlet-mockito)
    - [2.2 Test delle classi Model](#22-test-delle-classi-model)
    - [2.3 Test che toccano il DB](#23-test-che-toccano-il-db)
    - [2.4 Test di "documentazione"](#24-test-di-documentazione)
    - [2.5 Analisi statica del sorgente per i DAO](#25-analisi-statica-del-sorgente-per-i-dao)
        - [2.5.1 Perché l'analisi statica del sorgente per i DAO](#251-perché-lanalisi-statica-del-sorgente-per-i-dao)
    - [2.6 Test funzionali H2](#26-test-funzionali-h2)
- [3. Data Protection — 20 test (A02:2021)](#3-data-protection--20-test-a022021)
    - [3.1 Endpoint e funzionalità testate](#31-endpoint-e-funzionalità-testate)
    - [3.2 Miglioramenti implementati](#32-miglioramenti-implementati)
    - [3.3 Vulnerabilità documentate](#33-vulnerabilità-documentate)
- [4. Input Validation — 83 test (A03:2021)](#4-input-validation--83-test-a032021)
    - [4.1 Endpoint e funzionalità testate](#41-endpoint-e-funzionalità-testate)
    - [4.2 Miglioramenti implementati](#42-miglioramenti-implementati)
    - [4.3 Vulnerabilità documentate](#43-vulnerabilità-documentate)
    - [4.4 Dettaglio delle vulnerabilità risolte](#44-dettaglio-delle-vulnerabilità-risolte)
- [5. Authorization & Access Control — 75 test (A01:2021)](#5-authorization--access-control--75-test-a012021)
    - [5.1 Endpoint e funzionalità testate](#51-endpoint-e-funzionalità-testate)
    - [5.2 Miglioramenti implementati](#52-miglioramenti-implementati)
    - [5.3 Vulnerabilità risolte](#53-vulnerabilità-risolte)
    - [5.4 Vulnerabilità documentate (rischio accettato)](#54-vulnerabilità-documentate-rischio-accettato)
    - [5.5 Dettaglio delle vulnerabilità risolte](#55-dettaglio-delle-vulnerabilità-risolte)
- [6. Business Logic — 78 test (A04:2021)](#6-business-logic--78-test-a042021)
    - [6.1 Endpoint e funzionalità testate](#61-endpoint-e-funzionalità-testate)
    - [6.2 Miglioramenti implementati](#62-miglioramenti-implementati)
    - [6.3 Vulnerabilità documentate (rischio accettato)](#63-vulnerabilità-documentate-rischio-accettato)
    - [6.4 Dettaglio delle vulnerabilità risolte](#64-dettaglio-delle-vulnerabilità-risolte)
- [7. DAO Security — Analisi statica (A03:2021)](#7-dao-security--analisi-statica-a032021)
    - [7.1 Analisi statica del sorgente — 48 test](#71-analisi-statica-del-sorgente--48-test)
    - [7.2 Miglioramenti implementati](#72-miglioramenti-implementati)
    - [7.3 Finding documentati](#73-finding-documentati)
- [8. Riepilogo](#8-riepilogo)

---

## 1. Introduzione

La suite di test è stata progettata per fornire a SonarCloud la **coverage** necessaria al Quality Gate e, allo stesso tempo, per identificare vulnerabilità di sicurezza seguendo l'OWASP Testing Guide.

### 1.1 Strategia e organizzazione

I test sono organizzati in **5 aree OWASP**, ciascuna mappata sulle categorie dell'**OWASP Top 10 (2021)**:

| Area OWASP | OWASP Top 10 (2021) | Focus | Test |
|------------|:-------------------:|-------|:----:|
| **Data Protection** | A02:2021 — Cryptographic Failures | Hashing, credenziali, dati sensibili | 20 |
| **Input Validation** | A03:2021 — Injection | Sanitizzazione, XSS, SQL Injection | 83 |
| **Authorization & Access Control** | A01:2021 — Broken Access Control | Autenticazione, sessioni, privilegi | 75 |
| **Business Logic** | A04:2021 — Insecure Design | Logica applicativa, flussi, calcoli | 78 |
| **DAO Security** | A03:2021 — Injection | Query SQL, PreparedStatement, pattern statici | 48 |
| **TOTALE** | | | **304** |

> **Nota:** i **test funzionali H2** (176 test, 21 classi) sono documentati nel [report dedicato](functional.md).

### 1.2 Struttura delle directory dei test

```
src/test/java/
└── security/
    ├── unit/                          (29 file — test unitari)
    │   ├── authorization/             (7 file)
    │   ├── businesslogic/             (6 file)
    │   ├── daointegration/            (4 file — analisi statica)
    │   ├── dataprotection/            (3 file)
    │   └── inputvalidation/           (9 file — esclusi funzionali)
    └── functional/                    (test funzionali H2)
src/test/resources/
└── schema-h2.sql                      (schema DB H2)
```

### 1.3 Stack di test

| Dipendenza | Versione | Ruolo |
|------------|:--------:|-------|
| JUnit Jupiter API | 5.8.1 | Framework di test |
| JUnit Jupiter Engine | 5.8.1 | Esecuzione test |
| JUnit Jupiter Params | 5.8.1 | Supporto `@ParameterizedTest` |
| Mockito Core | 4.11.0 | Mock di `HttpServletRequest`, `HttpServletResponse`, `HttpSession` |
| AssertJ Core | 3.27.7 | Asserzioni fluent |
| Jsoup | 1.23.2 | Parsing HTML (per test JSP) |

**Nota:** JUnit e Mockito sono compatibili con Java 8 (source/target del progetto). I test funzionali usano H2 2.2.224 (vedi [functional.md](functional.md)).

---

## 2. Pattern di test utilizzati

### 2.1 Test dei Servlet (Mockito)

Le Servlet sono state testate invocando `service()` (metodo pubblico di `HttpServlet`) dopo aver configurato i mock necessari:

```java
servlet.service(request, response);
```

Mock obbligatori:
- `request.getMethod()` → `"GET"` o `"POST"`
- `request.getProtocol()` → `"HTTP/1.1"`
- `request.getSession()` e `request.getSession(false)` → mock di `HttpSession`

### 2.2 Test delle classi Model

Chiamate dirette ai metodi pubblici (`new Utente()`, `utente.setPassword(...)`, ecc.), senza necessità di mock.

### 2.3 Test che toccano il DB

Uso di `try/catch` per gestire l'eccezione `IllegalStateException` di `ConPool` (`MYSQL_PASSWORD` non configurata nei test). Questi test verificano il comportamento della Servlet **prima** della chiamata al database, documentando il flusso di validazione.

### 2.4 Test di "documentazione"

Alcuni test verificano che il codice **lanci un'eccezione** (NPE, NumberFormatException) o che **manchi una protezione**. Non sono test di successo, ma **evidenze di vulnerabilità documentate**:

- `testInputNullCausaNPE` in `RicercaServletTest`
- `testNumeroCartaNonValidoAccettato` in `AggiuntaProdottoServletTest`
- `testNessunControlloAutorizzazione` in `ModificaProdottiServletAmministratoreTest`

### 2.5 Analisi statica del sorgente per i DAO

Per i DAO, non testabili con il DB reale nell'ambiente JUnit, è stata adottata una strategia di **analisi statica del sorgente**: il file `.java` viene letto con `Files.readAllBytes()` e analizzato con espressioni regolari per verificare pattern di sicurezza.

Verifiche:

- Uso di `PreparedStatement` in tutti i metodi
- Assenza di concatenazione di stringhe nelle query (protezione SQL Injection)
- Presenza di `Statement.RETURN_GENERATED_KEYS`
- Uso di `try-with-resources` per la `Connection`

#### 2.5.1 Perché l'analisi statica del sorgente per i DAO

I DAO (Data Access Object) del progetto non sono testabili con il DB reale nell'ambiente JUnit, per tre motivi:

1. **Il DB è dentro Docker**: nei test unitari il container MySQL non è raggiungibile tramite l'host `db` (esiste solo nella rete Docker Compose).
2. **`ConPool` applica il fail-fast**: se `MYSQL_PASSWORD` non è impostata, `getConnection()` lancia `IllegalStateException`. Nei test la variabile non è definita (per sicurezza).
3. **I metodi dei DAO sono `static`**: Mockito 4.11.0 (l'unica versione compatibile con Java 8) non può mockare metodi statici.

Per questo è stata adottata la strategia di **analisi statica del sorgente**: i file `.java` dei DAO vengono letti con `Files.readAllBytes()` e analizzati con espressioni regolari per verificare i pattern di sicurezza rilevanti. Questa strategia copre esattamente ciò che Snyk Code e SonarCloud non rilevano per i DAO:

| Verifica | Rilevanza |
|----------|-----------|
| Uso di `PreparedStatement` in tutti i metodi | Protezione da SQL Injection |
| Assenza di concatenazione di stringhe nelle query | Anti-pattern noto |
| Uso di `Statement.RETURN_GENERATED_KEYS` | Corretto recupero degli ID |
| Uso di `try-with-resources` per la `Connection` | Prevenzione di resource leak |
| Assenza di `SELECT *` | Fragilità dello schema |
| Firma dei metodi (static, return type) | Coerenza dell'interfaccia |

### 2.6 Test funzionali H2

I test funzionali H2 (per DAO e Servlet) sono documentati nel **[report dedicato ai test funzionali](functional.md)**. Questo report copre esclusivamente i **test unitari** e l'**analisi statica del sorgente**.

**Perché i test funzionali sono separati:**
- Usano un **DB H2 in-memory** (infrastruttura aggiuntiva)
- Verificano il **comportamento a runtime** (diverso dall'analisi statica)
- Sono documentati con finding specifici (SEC-DAO-01, SEC-IV-03)

---

## 3. Data Protection — 20 test (A02:2021)

> **OWASP Top 10 (2021):** [A02:2021 — Cryptographic Failures](https://owasp.org/Top10/A02_2021-Cryptographic_Failures/)

**Obiettivo OWASP:** verificare che i dati sensibili (password, credenziali) siano protetti correttamente e che non ci siano esposizioni accidentali.

### 3.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità |
|--------|:----:|-------------------------|
| [`Utente.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/Utente.java) | 8 | `setPassword()` / `getPassword()` |
| [`ConPool.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/ConPool.java) | 7 | `getConnection()` (connessione DB) |
| [`ServletErrorHelper.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/ServletErrorHelper.java) | 5 | `sendError()` (gestione errori HTTP) |

**File di test:**
- [`PasswordHashingTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/unit/dataprotection/PasswordHashingTest.java)
- [`ConPoolTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/unit/dataprotection/ConPoolTest.java)
- [`ServletErrorHelperTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/unit/dataprotection/ServletErrorHelperTest.java)

### 3.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Hashing password** | SHA-1 senza salt (`MessageDigest.getInstance("SHA-1")`) | Documentato come rischio accettato (CWE-916) | Consapevolezza della vulnerabilità |
| **Credenziali DB** | Password hardcoded in `ConPool.java` | Lette da variabili d'ambiente (`System.getenv()`) | Fix GitGuardian |
| **Fail-fast** | Nessun controllo | `IllegalStateException` se `MYSQL_PASSWORD` non impostata | Prevenzione di avvio con configurazione incompleta |
| **Error handling** | `System.err.println` | `java.util.logging.Logger` con logging sicuro | No stack trace esposti |

### 3.3 Vulnerabilità documentate

- **CWE-916** ([Use of Password Hash With Insufficient Computational Effort](https://cwe.mitre.org/data/definitions/916.html)): SHA-1 senza salt per l'hashing delle password in `Utente.java`. **Rischio accettato** (vedi report Snyk Code).
- **Fix GitGuardian**: credenziali DB lette da variabili d'ambiente con fail-fast se `MYSQL_PASSWORD` non è impostata.

---

## 4. Input Validation — 83 test (A03:2021)

> **OWASP Top 10 (2021):** [A03:2021 — Injection](https://owasp.org/Top10/A03_2021-Injection/)

**Obiettivo OWASP:** verificare che gli input provenienti dall'utente siano validati e sanitizzati prima dell'uso, prevenendo XSS, SQL Injection e NPE.

### 4.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità | Fix applicato |
|--------|:----:|-------------------------|---------------|
| [`FiltraggioServletMateriale.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/FiltraggioServletMateriale.java) | 9 | `/FiltraggioServletMateriale?prodotto=...&materiale=...` | Sanitizzazione `replaceAll` |
| [`InizioServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/InizioServlet.java) | 9 | `/InizioServlet?action=...&valore=...` | Validazione regex |
| [`RicercaServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/RicercaServlet.java) | 6 | `/RicercaServlet?search=...` | Documenta NPE su input null |
| [`FiltraggioServletPrezzo.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/FiltraggioServletPrezzo.java) | 8 | `/FiltraggioServletPrezzo?prezzomin=...&prezzomax=...` | Documenta NumberFormatException |
| [`CarrelloServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/CarrelloServlet.java) | 9 | `/CarrelloServlet?action=...&quantita=...` | Documenta NPE e doppio forward |
| [`AggiuntaProdottoServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/AggiuntaProdottoServlet.java) | 11 | `/AggiuntaProdottoServlet` (17 parametri) | Documenta validazione assente |
| [`ModificaProdottiServletAmministratore.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/ModificaProdottiServletAmministratore.java) | 11 | `/ModificaProdottiServletAmministratore?nuovoPrezzo=...&quantitaTotale=...` | Documenta NPE e validazione assente |
| [`MaterialeServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/MaterialeServlet.java) | 9 | `/MaterialeServlet` (filtro materiale) | **Fix NPE + doppio forward** |
| [`JspHelper.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/JspHelper.java) | 9 | Estrazione dati per JSP | Estrazione sicura dati dalla request |

**File di test:** le 9 classi di test sono in [`src/test/java/security/unit/inputvalidation/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/unit/inputvalidation).

### 4.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Null check** | `mat.equalsIgnoreCase(...)` → NPE se `mat == null` | Null check esplicito | Crash prevention |
| **Sanitizzazione** | Input passato direttamente al DAO | `replaceAll("[^a-zA-Z0-9\\s]", "")` | XSS prevention |
| **Validazione regex** | Nessun controllo | `matches("[a-zA-Z0-9\\s]+")` | Input validation |
| **Doppio forward** | Forward eseguito due volte → `IllegalStateException` | `return` dopo il primo forward | Stability |
| **`getWriter()` IOException** | Non gestita | Gestita in `ServletErrorHelper` | Robustness |

### 4.3 Vulnerabilità documentate

- **NPE su input null**: documentata nei test (`testInputNullCausaNPE` in `RicercaServletTest`).
- **NumberFormatException**: documentata nei test (`testPrezzoNonNumericoCausaNumberFormatException` in `FiltraggioServletPrezzoTest`).
- **Validazione assente**: documentata nei test (`testInputNonValidatoPassaAlDao` in `AggiuntaProdottoServletTest`).

### 4.4 Dettaglio delle vulnerabilità risolte

| ID | Vulnerabilità | CWE | Endpoint | Test | Patch applicata |
|----|---------------|:---:|----------|------|-----------------|
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` quando `mat == null` | [CWE-476](https://cwe.mitre.org/data/definitions/476.html) | `/MaterialeServlet` | `testMatNullNonCausaNPE` | Aggiunto null check `if (mat == null) mat = ""` |
| SEC-IV-02 | Doppio forward quando `prodottiMateriale.isEmpty()` | [CWE-754](https://cwe.mitre.org/data/definitions/754.html) | `/MaterialeServlet` | `testNessunDoppioForward` | Aggiunto `return` dopo il primo forward |
| IV-01 | Sanitizzazione input assente | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) | `/FiltraggioServletMateriale` | `testSanitizationBeforeComparison` | Aggiunto `replaceAll("[^a-zA-Z0-9\\s]", "")` |
| IV-02 | Validazione regex assente | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | `/InizioServlet` | `testActionConScriptRestituisce400` | Aggiunto `matches("[a-zA-Z0-9\\s]+")` |
| IV-03 | NPE su input null | [CWE-476](https://cwe.mitre.org/data/definitions/476.html) | `/RicercaServlet` | `testInputNullCausaNPE` | Documentato (fix richiede null check) |
| IV-04 | NumberFormatException non gestita | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | `/FiltraggioServletPrezzo` | `testPrezzoNonNumericoCausaNumberFormatException` | Documentato |

---

## 5. Authorization & Access Control — 75 test (A01:2021)

> **OWASP Top 10 (2021):** [A01:2021 — Broken Access Control](https://owasp.org/Top10/A01_2021-Broken_Access_Control/)

**Obiettivo OWASP:** verificare che l'autenticazione e l'autorizzazione siano applicate correttamente, prevenendo accessi non autorizzati.

### 5.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint | Fix applicato |
|--------|:----:|----------|---------------|
| [`LoginServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/LoginServlet.java) | 11 | `/LoginServlet` (login, logout, carrello, riepilogo) | Documenta CSRF, brute-force, session fixation |
| [`RegistrazioneServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/RegistrazioneServlet.java) | 13 | `/RegistrazioneServlet` (12 parametri) | Documenta validazione assente |
| [`HomeServletAmministratore.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/HomeServletAmministratore.java) | 11 | `/HomeServletAmministratore?valore=...` | **CWE-862 (Broken Access Control)** |
| [`RendiAmministratoreServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/RendiAmministratoreServlet.java) | 12 | `/RendiAmministratoreServlet?action=...` | **CWE-862 + CWE-20** |
| [`PagamentoServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/PagamentoServlet.java) | 6 | `/PagamentoServlet` | **CWE-862** |
| [`CercaProdottoPerModificaServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/CercaProdottoPerModificaServlet.java) | 10 | `/CercaProdottoPerModificaServlet?search=...` | **CWE-862 + CWE-20** |
| [`RimuoviProdottoServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/RimuoviProdottoServlet.java) | 10 | `/RimuoviProdottoServlet` | **CWE-862** |

**File di test:** le 7 classi di test sono in [`src/test/java/security/unit/authorization/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/unit/authorization).

### 5.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **HomeServletAmministratore** | Accessibile a chiunque (anche anonimo) | Controllo `session.getAttribute("Amministratore")` + `isAmministratore()` | Broken Access Control risolto |
| **RendiAmministratoreServlet** | Chiunque poteva promuoversi ad admin | Controllo autorizzazione + `startsWith` invece di `contains` | Privilege Escalation risolto |
| **PagamentoServlet** | Accessibile senza login | Controllo `session.getAttribute("Utente")` | Missing Authorization risolto |
| **CercaProdottoPerModificaServlet** | Accessibile a chiunque | Controllo admin + validazione regex su `search` | Broken Access Control risolto |
| **RimuoviProdottoServlet** | Chiunque poteva cancellare prodotti | Controllo admin + null check su prodotto in sessione | Broken Access Control risolto |
| **RegistrazioneServlet** | Ruolo `amministratore` forzato a `false` | Forzatura esplicita `ps.setBoolean(13, false)` | Privilege Escalation prevention |
| **Validazione `action`** | `contains("amministratore")` → match permissivo | `startsWith("amministratore")` | Prevenzione di falsi positivi |

### 5.3 Vulnerabilità risolte

- **CWE-862** ([Missing Authorization](https://cwe.mitre.org/data/definitions/862.html)): accesso non autorizzato alle funzionalità amministrative. Risolto con controllo di autorizzazione in ogni Servlet protetta.
- **CWE-20** ([Improper Input Validation](https://cwe.mitre.org/data/definitions/20.html)): validazione dell'input assente in alcune Servlet. Risolto con regex e null check.

### 5.4 Vulnerabilità documentate (rischio accettato)

- **CWE-352** (CSRF token assente)
- **CWE-307** (Brute-force protection assente in `LoginServlet`)
- **CWE-384** (Session fixation in `LoginServlet`)

### 5.5 Dettaglio delle vulnerabilità risolte

| ID | Vulnerabilità | CWE | Endpoint | Test | Patch applicata |
|----|---------------|:---:|----------|------|-----------------|
| AUTH-01 | Accesso admin senza controllo | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/HomeServletAmministratore` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check `session.getAttribute("Amministratore")` |
| AUTH-02 | Privilege escalation via `?action=amministratore` | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/RendiAmministratoreServlet` | `testAnonimoNonPuoPromuovereAdmin` | Aggiunto check autorizzazione + `startsWith` |
| AUTH-03 | Pagamento accessibile senza login | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/PagamentoServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check `session.getAttribute("Utente")` |
| AUTH-04 | Accesso admin via `?valore=home` in HomeServlet | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/HomePage?valore=home` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check autorizzazione |
| AUTH-05 | Ricerca prodotto admin senza controllo | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/CercaProdottoPerModificaServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check admin + validazione `search` |
| AUTH-06 | Cancellazione prodotto senza controllo | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/RimuoviProdottoServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check admin + null check prodotto |
| AUTH-07 | Matching permissivo `contains` invece di `startsWith` | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | `/RendiAmministratoreServlet` | `testMatchingPermissivoBloccato` | Sostituito `contains` con `startsWith` |

---

## 6. Business Logic — 78 test (A04:2021)

> **OWASP Top 10 (2021):** [A04:2021 — Insecure Design](https://owasp.org/Top10/A04_2021-Insecure_Design/)

**Obiettivo OWASP:** verificare che la logica applicativa (calcoli, flussi, validazioni di dominio) sia corretta e non manipolabile.

### 6.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità | Fix applicato |
|--------|:----:|-------------------------|---------------|
| [`Prodotto.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/Prodotto.java) | 18 | Getter/setter, validazione | Documenta validazione assente |
| [`AcquistoProdotti.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/AcquistoProdotti.java) | 18 | Getter/setter, mutazione condivisa | Documenta mutazione condivisa |
| [`Carta.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/Carta.java) | 17 | Getter/setter, dati carta | Documenta violazione PCI DSS |
| [`DatiPagamentoServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/DatiPagamentoServlet.java) | 10 | `/DatiPagamentoServlet` (pagamento) | **CWE-862** |
| [`HomeServlet.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/HomeServlet.java) | 10 | `/HomePage?valore=home` | **CWE-862** |
| [`Registrazione.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/Registrazione.java) | 5 | `/Registrazione` (form) | Servlet di sola presentazione |

**File di test:** le 6 classi di test sono in [`src/test/java/security/unit/businesslogic/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/unit/businesslogic).

### 6.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **HomeServlet** | `?valore=home` accessibile a chiunque | Controllo autorizzazione admin | Broken Access Control risolto |
| **DatiPagamentoServlet** | Accessibile senza login, carrello non verificato | Controllo autenticazione + carrello non vuoto | Missing Authorization risolto |
| **DatiPagamentoServlet** | Carrello svuotato anche se pagamento fallisce | Documentato come bug (mancanza transazione atomica) | Consapevolezza |
| **Carta** | CVV e numero carta in chiaro | Documentato come violazione PCI DSS | Consapevolezza |
| **AcquistoProdotti** | Mutazione condivisa (setter modificano oggetti originali) | Documentato | Consapevolezza |
| **Prodotto** | Setter senza validazione | Documentato | Consapevolezza |

### 6.3 Vulnerabilità documentate (rischio accettato)

- **PCI DSS**: CVV e numero carta memorizzati in chiaro in `Carta.java` (fix richiede cifratura e refactoring).
- **Mutazione condivisa**: in `AcquistoProdotti` e `Carta` i setter modificano gli oggetti originali.
- **Validazione assente**: sui setter di `Prodotto`.
- **Mancanza transazione atomica**: in `DatiPagamentoServlet`.

### 6.4 Dettaglio delle vulnerabilità risolte

| ID | Vulnerabilità | CWE | Endpoint | Test | Patch applicata |
|----|---------------|:---:|----------|------|-----------------|
| BL-01 | Accesso admin via `?valore=home` senza controllo | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/HomePage?valore=home` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check autorizzazione |
| BL-02 | Pagamento accessibile senza login/carrello vuoto | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) | `/DatiPagamentoServlet` | `testAnonimoSessioneNullRiceve403` | Controllo autenticazione + carrello |

---

## 7. DAO Security — Analisi statica (A03:2021)

> **OWASP Top 10 (2021):** [A03:2021 — Injection](https://owasp.org/Top10/A03_2021-Injection/)

**Obiettivo OWASP:** verificare che le query SQL siano protette da SQL Injection tramite **analisi statica del sorgente** (patterns di sicurezza testuali).

> **Nota:** i test funzionali H2 dei DAO (38 test) e delle Servlet (136 test) sono documentati nel [report dei test funzionali](functional.md).

### 7.1 Analisi statica del sorgente — 48 test

**File di test:** [`src/test/java/security/unit/daointegration/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/unit/daointegration)

| Classe | Test | Query / Operazioni | Note |
|--------|:----:|---------------------|------|
| [`ProdottoDAO.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/ProdottoDAO.java) | 10 | `SELECT`, `INSERT`, `UPDATE`, `DELETE` | Usa PreparedStatement, no SQL Injection |
| [`UtenteDAO.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/UtenteDAO.java) | 13 | `SELECT`, `INSERT`, `UPDATE` (login, registrazione) | Documenta SHA1 nel login (CWE-916) |
| [`AcquistoProdottiDAO.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/AcquistoProdottiDAO.java) | 13 | `SELECT`, `INSERT` (acquisti) | Non estende HttpServlet (buona pratica) |
| [`CartaDAO.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/CartaDAO.java) | 12 | `SELECT`, `INSERT` (carte) | Documenta SELECT senza WHERE, CVV in chiaro |

**Verifiche effettuate:**
- Uso di `PreparedStatement` in tutti i metodi
- Assenza di concatenazione di stringhe nelle query
- Presenza di `Statement.RETURN_GENERATED_KEYS`
- Uso di `try-with-resources` per la `Connection`
- Assenza di `SELECT *`
- Firma dei metodi (static, return type)

### 7.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **SQL Injection** | Query con concatenazione di stringhe | `PreparedStatement` con placeholder `?` | SQL Injection prevention |
| **Resource leak** | `Connection` non chiusa | `try-with-resources` (esteso a `PreparedStatement` e `ResultSet`) | Resource management |
| **ID generati** | Nessun recupero | `Statement.RETURN_GENERATED_KEYS` | Corretto recupero ID |
| **Eccezioni generiche** | `RuntimeException` generica | `DataAccessException` custom | Migliore semantica dell'errore |
| **SHA-1 nel login** | `SHA1()` nel database | Documentato come rischio accettato (CWE-916) | Consapevolezza |
| **SELECT senza WHERE** (CartaDAO) | Carica tutte le carte in memoria | Documentato | Consapevolezza |
| **CVV/numero carta in chiaro** | Salvati in chiaro in `CartaDiCredito` | Documentato | Consapevolezza PCI-DSS |
| **Design smell** | DAO estendono `HttpServlet` | Documentato | Consapevolezza |

### 7.3 Finding documentati

| ID | Vulnerabilità | CWE | Classe | Stato |
|----|---------------|:---:|--------|:-----:|
| SEC-DAO-02 | Numero carta e CVV salvati in chiaro nella tabella `CartaDiCredito`. Il CVV non dovrebbe **mai** essere persistito (PCI DSS 3.2) | [CWE-312](https://cwe.mitre.org/data/definitions/312.html) | `CartaDAO` | 📝 Documentato |
| SEC-DAO-03 | Il setter `Utente.setPassword()` applica SHA-1: ogni lettura dal DB produce un doppio hash `SHA1(SHA1(pwd))` | [CWE-1064](https://cwe.mitre.org/data/definitions/1064.html) | `Utente.java` | 📝 Documentato |
| SEC-DAO-04 | `UtenteDAO.doLogin` usa `SHA1()` per confrontare la password nel database | [CWE-916](https://cwe.mitre.org/data/definitions/916.html) | `UtenteDAO` | 📝 Documentato |
| SEC-DAO-05 | `CartaDAO.aggiuntaCredenzialiPagamento` esegue `SELECT numeroCarta FROM CartaDiCredito` senza WHERE, caricando tutte le carte in memoria | [CWE-770](https://cwe.mitre.org/data/definitions/770.html) | `CartaDAO` | 📝 Documentato |
| SEC-DAO-06 | I DAO `ProdottoDAO` e `UtenteDAO` estendono `HttpServlet` (design smell: un DAO non è una Servlet) | — | `ProdottoDAO`, `UtenteDAO` | 📝 Documentato |
| SEC-DAO-07 | Eccezioni SQL wrappate in `DataAccessException` custom (miglioramento rispetto a `RuntimeException` generica) | [CWE-391](https://cwe.mitre.org/data/definitions/391.html) | Tutti i DAO | ✅ Risolto |

**Nota su SEC-DAO-03:** il doppio hashing non compromette il login (perché `doLogin` confronta l'hash nel DB con `SHA1(input)` prima del re-hashing del bean), ma è un **design smell** che viola il principio di separazione tra Model e hashing. Un setter non dovrebbe mai applicare trasformazioni crittografiche.

**Nota:** il trattino `—` nella colonna CWE indica un **design smell** o problema architetturale che non mappa su un CWE specifico (non è una vulnerabilità di sicurezza ma una violazione di best practice).

> **Nota:** il finding **SEC-DAO-01** (`doRetriveBySearch` non case-insensitive) è stato scoperto **solo** grazie ai test funzionali H2, ed è documentato nel [report dei test funzionali](functional.md).

---

## 8. Riepilogo

### 8.1 Metriche

| Metrica | Valore |
|:---|:---:|
| **Test unitari totali** | **304** |
| **Classi di test** | **29** |
| **Aree OWASP** | **5** |
| **Success rate** | **100%**|
| **Test funzionali H2** | **176** (vedi [functional.md](functional.md)) |
| **TOTALE PROGETTO** | **480** |

### 8.2 Copertura per Area OWASP e Top 10 (2021)

| Area OWASP | OWASP Top 10 (2021) | Classi | Test |
|------------|:-------------------:|:------:|:----:|
| Data Protection | A02:2021 — Cryptographic Failures | 3 | 20 |
| Input Validation | A03:2021 — Injection | 9 | 83 |
| Authorization | A01:2021 — Broken Access Control | 7 | 75 |
| Business Logic | A04:2021 — Insecure Design | 6 | 78 |
| DAO Security — statici | A03:2021 — Injection | 4 | 48 |
| **TOTALE UNITARI** | | **29** | **304** |
| DAO Security — funzionali (H2) | A03:2021 — Injection | 4 | 38 |
| Servlet funzionali (H2) | A01/A03/A04 | 16 | 136 |
| Exceptions | — | 1 | 2 |
| **TOTALE FUNZIONALI** | | **21** | **176** |
| **TOTALE PROGETTO** | | **50** | **480** |


### 8.3 Finding Principali

| ID | Vulnerabilità | CWE | Stato |
|----|---------------|:---:|:-----:|
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` | CWE-476 | Risolto |
| SEC-IV-02 | Doppio forward in `MaterialeServlet` | CWE-754 | Risolto |
| IV-01 | Sanitizzazione input assente | CWE-79 | Risolto |
| IV-02 | Validazione regex assente | CWE-20 | Risolto |
| IV-03 | NPE su input null | CWE-476 | Documentato |
| IV-04 | NumberFormatException non gestita | CWE-20 | Documentato |
| AUTH-01 ÷ AUTH-06 | Missing Authorization | CWE-862 | Risolto |
| AUTH-07 | Matching permissivo | CWE-20 | Risolto |
| BL-01, BL-02 | Accesso non autorizzato | CWE-862 | Risolto |
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive | CWE-178 | Risolto (vedi [functional.md](functional.md)) |
| SEC-DAO-02 | Numero carta/CVV in chiaro | CWE-312 | Documentato |
| SEC-DAO-03 | Doppio hashing nel setter `Utente.setPassword()` | CWE-1064 | Documentato |
| SEC-DAO-04 | `doLogin` usa `SHA1()` | CWE-916 | Documentato |
| SEC-DAO-05 | `CartaDAO` SELECT senza WHERE | CWE-770 | Documentato |
| SEC-DAO-06 | DAO estendono `HttpServlet` (design smell) | — | Documentato |
| SEC-DAO-07 | Eccezioni SQL wrappate in `DataAccessException` | CWE-391 | Risolto |
| SEC-IV-03 | Doppio forward in `FiltraggioServletPrezzo` | CWE-754 | Documentato (vedi [functional.md](functional.md)) |
| ACC-01 | SHA-1 per password (rischio accettato) | CWE-916 | **Accettato** |
| ACC-02 | CSRF assente (rischio accettato) | CWE-352 | **Accettato**|
| ACC-03 | Brute-force protection assente (rischio accettato) | CWE-307 | **Accettato**|
| ACC-04 | Session fixation (rischio accettato) | CWE-384 | **Accettato**|

### 8.4 Nota sul conteggio

Il totale comprende le esecuzioni multiple dei test parametrizzati (`@ParameterizedTest`) in `HomeServletAmministratoreTest`, `CercaProdottoPerModificaServletTest`, `RendiAmministratoreServletTest`, `AggiuntaProdottoServletTest`, `InizioServletTest` e `ModificaProdottiServletAmministratoreTest`.

### 8.5 Nota metodologica finale

Questo report copre **tre strategie di verifica**:

1. **Test diretti** per i Bean (nessuna infrastruttura)
2. **Mockito** per le Servlet (simulazione del web container)
3. **Analisi statica** del sorgente per i DAO

A queste si aggiunge **SonarQube** per la verifica statica sull'intero codebase. La **terza strategia** — **test funzionali H2** — è documentata separatamente nel [report dei test funzionali](functional.md).

Ogni strategia copre aspetti diversi e **non sono ridondanti**: il valore aggiunto è dimostrato dal finding SEC-DAO-01, scoperto solo grazie ai test funzionali H2 e documentato nel report dedicato.