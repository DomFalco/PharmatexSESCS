# Test di Sicurezza — Suite OWASP

**Strategia:** [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)

**Framework:** JUnit 5 + Mockito 4.11.0 + AssertJ 3.24.2 + H2 2.2.224

**Test totali:** 339 (33 classi, 5 aree OWASP)

**Data ultima esecuzione:** 07/10/2026

> Questo documento descrive in dettaglio la suite di test di sicurezza del progetto. La suite alimenta la **coverage** sul New Code, requisito per il Quality Gate SonarCloud (vedi il [report SonarCloud](../SonarQube/README.md)).

---

## Sommario Esecutivo

Il lavoro ha portato alla **risoluzione di 14 vulnerabilità** — principalmente [CWE-862](https://cwe.mitre.org/data/definitions/862.html) (Missing Authorization), [CWE-20](https://cwe.mitre.org/data/definitions/20.html) (Improper Input Validation), [CWE-79](https://cwe.mitre.org/data/definitions/79.html) (XSS), [CWE-476](https://cwe.mitre.org/data/definitions/476.html) (NPE) e [CWE-178](https://cwe.mitre.org/data/definitions/178.html) (Improper Handling of Case Sensitivity) — e alla **documentazione di ulteriori ~15 finding** classificati come **rischio accettato**, tra cui [CWE-916](https://cwe.mitre.org/data/definitions/916.html) (SHA-1 per password), [CWE-352](https://cwe.mitre.org/data/definitions/352.html) (CSRF), [CWE-307](https://cwe.mitre.org/data/definitions/307.html) (brute-force), [CWE-384](https://cwe.mitre.org/data/definitions/384.html) (session fixation) e violazioni PCI DSS.

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
    - [2.6 Test funzionali H2 in-memory per i DAO](#26-test-funzionali-h2-in-memory-per-i-dao)
        - [2.6.1 Perché H2 solo per i DAO](#261-perché-h2-solo-per-i-dao)
        - [2.6.2 Architettura dell'infrastruttura H2](#262-architettura-dellinfrastruttura-h2)
- [3. Data Protection — 20 test (A02:2021)](#3-data-protection--20-test-a022021)
    - [3.1 Endpoint e funzionalità testate](#31-endpoint-e-funzionalità-testate)
    - [3.2 Miglioramenti implementati](#32-miglioramenti-implementati)
    - [3.3 Vulnerabilità documentate](#33-vulnerabilità-documentate)
- [4. Input Validation — 81 test (A03:2021)](#4-input-validation--81-test-a032021)
    - [4.1 Endpoint e funzionalità testate](#41-endpoint-e-funzionalità-testate)
    - [4.2 Miglioramenti implementati](#42-miglioramenti-implementati)
    - [4.3 Vulnerabilità documentate](#43-vulnerabilità-documentate)
    - [4.4 Dettaglio delle vulnerabilità risolte](#44-dettaglio-delle-vulnerabilità-risolte)
- [5. Authorization & Access Control — 73 test (A01:2021)](#5-authorization--access-control--73-test-a012021)
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
- [7. DAO Security — 48 statici + 35 funzionali (A03:2021)](#7-dao-security--48-statici--35-funzionali-a032021)
    - [7.1 Analisi statica del sorgente — 48 test](#71-analisi-statica-del-sorgente--48-test)
    - [7.2 Test funzionali H2 — 35 test](#72-test-funzionali-h2--35-test)
    - [7.3 Miglioramenti implementati](#73-miglioramenti-implementati)
    - [7.4 Dettaglio dei finding](#74-dettaglio-dei-finding)
- [8. Riepilogo](#8-riepilogo)

---

## 1. Introduzione

La suite di test è stata progettata per fornire a SonarCloud la **coverage** necessaria al Quality Gate e, allo stesso tempo, per identificare vulnerabilità di sicurezza seguendo l'OWASP Testing Guide.

### 1.1 Strategia e organizzazione

I test sono organizzati in **5 aree OWASP**, ciascuna mappata sulle categorie dell'**OWASP Top 10 (2021)**:

| Area OWASP | OWASP Top 10 (2021) | Focus | Test |
|------------|:-------------------:|-------|:----:|
| **Data Protection** | A02:2021 — Cryptographic Failures | Hashing, credenziali, dati sensibili | 20 |
| **Input Validation** | A03:2021 — Injection | Sanitizzazione, XSS, SQL Injection | 81 |
| **Authorization & Access Control** | A01:2021 — Broken Access Control | Autenticazione, sessioni, privilegi | 73 |
| **Business Logic** | A04:2021 — Insecure Design | Logica applicativa, flussi, calcoli | 78 |
| **DAO Security** | A03:2021 — Injection | Query SQL, PreparedStatement, comportamento runtime | 83 |
| **TOTALE** | | | **339** |

### 1.2 Struttura delle directory dei test

```
src/test/java/
└── security/
    ├── inputvalidation/      (9 file)
    ├── authorization/        (7 file)
    ├── dataprotection/       (3 file)
    ├── businesslogic/        (6 file)
    ├── daointegration/       (4 file — analisi statica)
    └── daofunctional/        (6 file — H2 in-memory)
src/test/resources/
└── schema-h2.sql             (schema DB H2)
```

### 1.3 Stack di test

| Dipendenza | Versione | Ruolo |
|------------|:--------:|-------|
| JUnit Jupiter API | 5.8.1 | Framework di test |
| JUnit Jupiter Engine | 5.8.1 | Esecuzione test |
| JUnit Jupiter Params | 5.8.1 | Supporto `@ParameterizedTest` |
| Mockito Core | 4.11.0 | Mock di `HttpServletRequest`, `HttpServletResponse`, `HttpSession` |
| AssertJ Core | 3.24.2 | Asserzioni fluent |
| Jsoup | 1.17.2 | Parsing HTML (per test JSP) |
| **H2 Database** | **2.2.224** | **DB in-memory per i test funzionali dei DAO** |

**Nota:** JUnit e Mockito sono compatibili con Java 8 (source/target del progetto). H2 2.2.224 richiede Java 11+, ma viene usato solo con scope `test` e con il JDK del runner CI (Java 21+).

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

Per i DAO, non testabili con il DB reale nell'ambiente JUnit, è stata adottata inizialmente una strategia di **analisi statica del sorgente**: il file `.java` viene letto con `Files.readAllBytes()` e analizzato con espressioni regolari per verificare pattern di sicurezza.

Verifiche:

- Uso di `PreparedStatement` in tutti i metodi
- Assenza di concatenazione di stringhe nelle query (protezione SQL Injection)
- Presenza di `Statement.RETURN_GENERATED_KEYS`
- Uso di `try-with-resources` per la `Connection`

#### 2.5.1 Perché l'analisi statica del sorgente per i DAO

I DAO (Data Access Object) del progetto non erano testabili con il DB reale nell'ambiente JUnit, per tre motivi:

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

**Alternativa considerata e scartata inizialmente:** test con DB reale (Testcontainers) o in-memory (H2). Testcontainers avrebbe richiesto Docker nel runner CI (complessità + lentezza). H2 avrebbe richiesto di riscrivere le query MySQL-specifiche (es. `SHA1()`, `LIMIT`).

**Conclusione:** l'analisi statica del sorgente è la strategia ottimale per questo contesto — verifica i pattern di sicurezza senza richiedere un DB, ed è eseguibile nella pipeline CI/CD.

### 2.6 Test funzionali H2 in-memory per i DAO

In aggiunta all'analisi statica, è stata introdotta una suite di **test funzionali** che esegue i DAO contro un database **H2 in-memory** configurato in modalità MySQL. Questo permette di verificare il **comportamento runtime** delle query, cosa che né SonarQube né l'analisi statica possono fare.

**Vantaggi rispetto all'analisi statica:**

| Aspetto | Analisi statica | Test funzionali H2 |
|---|:---:|:---:|
| Verifica comportamento runtime | No | Sì |
| Verifica INSERT/SELECT/UPDATE/DELETE | No | Sì |
| Verifica SQL Injection a runtime | No | Sì |
| Contribuisce alla coverage SonarCloud | No | Sì |
| Trova bug di comportamento | No | Sì |

**Valore aggiunto:** il test `testDoRetriveBySearch_CaseInsensitive` ha scoperto il bug **SEC-DAO-01** (query `upper(nomeProd) LIKE ?` ma pattern non uppercasato lato Java), invisibile a SonarQube e all'analisi statica. Dopo il fix, il test verifica il comportamento corretto.

#### 2.6.1 Perché H2 solo per i DAO

La scelta di applicare H2 **solo ai DAO** è metodologica, non di comodo. Ogni componente è testato con lo strumento adatto al suo livello di astrazione:

| Componente | Approccio | Motivo |
|---|---|---|
| **Bean / Model** (`Utente`, `Prodotto`, `Carta`) | Test diretti (nessun mock) | Sono POJO: getter/setter e logica semplice, nessuna infrastruttura |
| **Servlet** (`LoginServlet`, `HomeServlet`, ecc.) | Mockito | Testano la logica di controllo (autorizzazione, redirect, forward). Non parlano col DB direttamente, ma coi DAO. Mockarli è la scelta corretta per un test unitario isolato |
| **DAO** (`ProdottoDAO`, `UtenteDAO`, ecc.) | H2 in-memory | Eseguono SQL vero contro un DB. Non sono mockabili (metodi statici). H2 è l'unico modo per verificarne il comportamento senza un DB reale |
| **Pattern di sicurezza** | SonarQube + analisi statica | Coprono la "forma" del codice, non il comportamento |

**Conseguenza:** le Servlet **non** vanno riscritti con H2 — i loro test attuali (Mockito) sono corretti. I Bean **non** vanno riscritti — i loro test diretti sono corretti. Solo i DAO richiedevano un'aggiunta funzionale, perché erano l'unico componente che "tocca il DB" ed era testato solo staticamente.

#### 2.6.2 Architettura dell'infrastruttura H2

L'infrastruttura H2 è composta da **3 file di supporto** e **4 file di test**:

| File | Ruolo |
|---|---|
| [`schema-h2.sql`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/resources/schema-h2.sql) | Schema DB (adattato da `Pharmatex.sql`) compatibile con H2 `MODE=MySQL` |
| [`TestFunctions.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/TestFunctions.java) | Implementazione Java di `SHA1()`, registrata come alias di funzione in H2 |
| [`BaseH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/BaseH2Test.java) | Classe base astratta: attiva il test mode, esegue lo schema, pulisce il DB prima di ogni test |
| [`ProdottoDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/ProdottoDAOH2Test.java) | Test funzionali di `ProdottoDAO` (11 test) |
| [`UtenteDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/UtenteDAOH2Test.java) | Test funzionali di `UtenteDAO` (13 test) |
| [`CartaDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/CartaDAOH2Test.java) | Test funzionali di `CartaDAO` (4 test) |
| [`AcquistoProdottiDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/AcquistoProdottiDAOH2Test.java) | Test funzionali di `AcquistoProdottiDAO` (7 test) |

**Modifica al codice di produzione:** `ConPool.java` è stato esteso con 3 elementi **additivi** (nessuna modifica alla logica esistente):

```java
private static boolean testMode = false;
private static String testUrl = null;

public static void enableTestMode(String url) { /* attiva H2 */ }
public static void disableTestMode() { /* ripristina MySQL */ }
```

Il branch `if (testMode)` in `getConnection()` è **irraggiungibile in produzione** (nessuno chiama `enableTestMode()` fuori dai test).

**Registrazione della funzione SHA1:** poiché `UtenteDAO.doLogin` usa `SHA1(?)` (funzione MySQL-specifica), in H2 è stato registrato un alias:

```sql
CREATE ALIAS IF NOT EXISTS SHA1 FOR "security.daofunctional.TestFunctions.sha1"
```

Da quel momento, ogni chiamata a `SHA1(x)` in H2 viene tradotta nella chiamata al metodo Java `TestFunctions.sha1(x)`, che usa `MessageDigest.getInstance("SHA-1")` — la stessa implementazione del bean `Utente`.

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
- [`PasswordHashingTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/dataprotection/PasswordHashingTest.java)
- [`ConPoolTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/dataprotection/ConPoolTest.java)
- [`ServletErrorHelperTest.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/dataprotection/ServletErrorHelperTest.java)

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

## 4. Input Validation — 81 test (A03:2021)

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

**File di test:** le 9 classi di test sono in [`src/test/java/security/inputvalidation/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/inputvalidation).

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
| SEC-IV-02 | Doppio forward quando `prodottiMateriale.isEmpty()` | — | `/MaterialeServlet` | `testNessunDoppioForward` | Aggiunto `return` dopo il primo forward |
| IV-01 | Sanitizzazione input assente | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) | `/FiltraggioServletMateriale` | `testSanitizationBeforeComparison` | Aggiunto `replaceAll("[^a-zA-Z0-9\\s]", "")` |
| IV-02 | Validazione regex assente | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | `/InizioServlet` | `testActionConScriptRestituisce400` | Aggiunto `matches("[a-zA-Z0-9\\s]+")` |
| IV-03 | NPE su input null | [CWE-476](https://cwe.mitre.org/data/definitions/476.html) | `/RicercaServlet` | `testInputNullCausaNPE` | Documentato (fix richiede null check) |
| IV-04 | NumberFormatException non gestita | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | `/FiltraggioServletPrezzo` | `testPrezzoNonNumericoCausaNumberFormatException` | Documentato |

---

## 5. Authorization & Access Control — 73 test (A01:2021)

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

**File di test:** le 7 classi di test sono in [`src/test/java/security/authorization/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/authorization).

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

**File di test:** le 6 classi di test sono in [`src/test/java/security/businesslogic/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/businesslogic).

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

## 7. DAO Security — 48 statici + 35 funzionali (A03:2021)

> **OWASP Top 10 (2021):** [A03:2021 — Injection](https://owasp.org/Top10/A03_2021-Injection/)

**Obiettivo OWASP:** verificare che le query SQL siano protette da SQL Injection e che l'accesso al DB sia sicuro, sia **staticamente** (pattern) sia **funzionalmente** (comportamento runtime).

La sezione DAO adotta una **strategia a due livelli**:

| Livello | Approccio | Cosa verifica | Test |
|---------|-----------|---------------|:----:|
| **Statico** (`daointegration/`) | Analisi del sorgente con regex | Pattern: PreparedStatement ovunque, no concatenazione, RETURN_GENERATED_KEYS, try-with-resources | 48 |
| **Funzionale** (`daofunctional/`) | H2 in-memory | Comportamento runtime: INSERT/SELECT/UPDATE/DELETE, SQL Injection a runtime, gestione SHA1, dati sensibili in chiaro | 35 |
| **TOTALE** | | | **83** |

### 7.1 Analisi statica del sorgente — 48 test

**File di test:** [`src/test/java/security/daointegration/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/daointegration)

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

### 7.2 Test funzionali H2 — 35 test

**File di test:** [`src/test/java/security/daofunctional/`](https://github.com/DomFalco/PharmatexSESCS/tree/master/src/test/java/security/daofunctional)

| Classe di test | Test | Cosa verifica |
|----------------|:----:|---------------|
| [`ProdottoDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/ProdottoDAOH2Test.java) | 11 | Lettura (SELECT), scrittura (INSERT/UPDATE/DELETE), filtro, SQL Injection, fix SEC-DAO-01 |
| [`UtenteDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/UtenteDAOH2Test.java) | 13 | Login con SHA1, controllo email, registrazione, promozione/declassamento admin, SQL Injection |
| [`CartaDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/CartaDAOH2Test.java) | 4 | Inserimento carta, anti-duplicati, dati sensibili in chiaro (PCI-DSS) |
| [`AcquistoProdottiDAOH2Test.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/test/java/security/daofunctional/AcquistoProdottiDAOH2Test.java) | 7 | INSERT acquisti, JOIN con cliente, integrità referenziale |

**Esempio di test funzionale:**

```java
@Test
@DisplayName("SQL Injection su doRetriveBySearch viene neutralizzata dal PreparedStatement")
void testSqlInjectionNeutralizzata() throws Exception {
    executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

    Prodotto result = ProdottoDAO.doRetriveBySearch("' OR '1'='1");

    assertThat(result.getIdProdotto()).isNull();
}
```

### 7.3 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **SQL Injection** | Query con concatenazione di stringhe | `PreparedStatement` con placeholder `?` | SQL Injection prevention |
| **Resource leak** | `Connection` non chiusa | `try-with-resources` | Resource management |
| **ID generati** | Nessun recupero | `Statement.RETURN_GENERATED_KEYS` | Corretto recupero ID |
| **Case sensitivity** (`doRetriveBySearch`) | `upper(nomeProd) LIKE ?` (pattern non uppercasato) | `upper(nomeProd) LIKE upper(?)` | **Fix SEC-DAO-01** |
| **SHA-1 nel login** | `SHA1()` nel database | Documentato come rischio accettato (CWE-916) | Consapevolezza |
| **SELECT senza WHERE** (CartaDAO) | Carica tutte le carte in memoria | Documentato | Consapevolezza |
| **CVV/numero carta in chiaro** | Salvati in chiaro in `CartaDiCredito` | Documentato | Consapevolezza PCI-DSS |
| **Design smell** | DAO estendono `HttpServlet` | Documentato | Consapevolezza |

### 7.4 Dettaglio dei finding

#### 7.4.1 Vulnerabilità risolte

| ID | Vulnerabilità | CWE | Classe | Test | Patch applicata |
|----|---------------|:---:|--------|------|-----------------|
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive: la query usava `upper(nomeProd) LIKE ?` senza uppercasare il pattern | [CWE-178](https://cwe.mitre.org/data/definitions/178.html) | `ProdottoDAO` | `testDoRetriveBySearch_CaseInsensitive` | Query cambiata in `upper(nomeProd) LIKE upper(?)` |

**Nota su SEC-DAO-01:** il bug è stato scoperto **solo** grazie al test funzionale su H2. Né SonarQube né l'analisi statica del sorgente potevano rilevarlo, poiché si tratta di un bug di **comportamento a runtime** (logica case-sensitive nonostante l'intento case-insensitive), non di un pattern statico rilevabile. Questo conferma il valore aggiunto dell'approccio funzionale nel colmare il gap tra analisi statica e verifica comportamentale.

#### 7.4.2 Vulnerabilità documentate (rischio accettato)

| ID | Vulnerabilità | CWE | Classe | Test | Stato |
|----|---------------|:---:|--------|------|:-----:|
| SEC-DAO-02 | Numero carta e CVV salvati in chiaro nella tabella `CartaDiCredito`. Il CVV in particolare non dovrebbe **mai** essere persistito (PCI DSS 3.2) | [CWE-312](https://cwe.mitre.org/data/definitions/312.html) | `CartaDAO` | `testNumeroCartaInChiaro` | 📝 Documentato |
| SEC-DAO-03 | Il setter `Utente.setPassword()` applica SHA-1: ogni lettura dal DB produce un doppio hash `SHA1(SHA1(pwd))` | [CWE-1064](https://cwe.mitre.org/data/definitions/1064.html) | `Utente.java` | `testDoRetriveUtente` | 📝 Documentato |
| — | `UtenteDAO.doLogin` usa `SHA1()` per confrontare la password (già documentato in Data Protection) | [CWE-916](https://cwe.mitre.org/data/definitions/916.html) | `UtenteDAO` | Vari | 📝 Documentato |
| — | `CartaDAO.aggiuntaCredenzialiPagamento` esegue `SELECT numeroCarta FROM CartaDiCredito` senza WHERE, caricando tutte le carte in memoria | [CWE-770](https://cwe.mitre.org/data/definitions/770.html) | `CartaDAO` | Vari | 📝 Documentato |
| — | I DAO `ProdottoDAO` e `UtenteDAO` estendono `HttpServlet` (design smell: un DAO non è un Servlet) | — | `ProdottoDAO`, `UtenteDAO` | — | 📝 Documentato |
| — | Eccezioni SQL wrappate in `RuntimeException` generico senza logging strutturato | [CWE-391](https://cwe.mitre.org/data/definitions/391.html) | Tutti i DAO | — | 📝 Documentato |

**Nota su SEC-DAO-03:** il doppio hashing non compromette il login (perché `doLogin` confronta l'hash nel DB con `SHA1(input)` prima del re-hashing del bean), ma è un **design smell** che viola il principio di separazione tra Model e hashing. Un setter non dovrebbe mai applicare trasformazioni crittografiche.

---

## 8. Riepilogo

### 8.1 Metriche

| Metrica | Valore |
|:---|:---:|
| **Test totali** | **339** |
| **Classi di test** | **33** |
| **Aree OWASP** | **5** |
| **Success rate** | **100%**  |
| **Vulnerabilità risolte** | **14** |
| **Vulnerabilità documentate (rischio accettato)** | **~15** |

### 8.2 Copertura per Area OWASP e Top 10 (2021)

| Area OWASP | OWASP Top 10 (2021) | Classi | Test |
|------------|:-------------------:|:------:|:----:|
| Data Protection | A02:2021 — Cryptographic Failures | 3 | 20 |
| Input Validation | A03:2021 — Injection | 9 | 81 |
| Authorization | A01:2021 — Broken Access Control | 7 | 73 |
| Business Logic | A04:2021 — Insecure Design | 6 | 78 |
| DAO Security — statici | A03:2021 — Injection | 4 | 48 |
| DAO Security — funzionali (H2) | A03:2021 — Injection | 6 | 35 |
| **TOTALE** | | **35** | **339** |

### 8.3 Finding Principali

| ID | Vulnerabilità | CWE | Stato |
|----|---------------|:---:|:-----:|
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` | CWE-476 | Risolto |
| SEC-IV-02 | Doppio forward | — | Risolto |
| IV-01 | Sanitizzazione input assente | CWE-79 | Risolto |
| IV-02 | Validazione regex assente | CWE-20 | Risolto |
| IV-03 | NPE su input null | CWE-476 | Documentato |
| IV-04 | NumberFormatException non gestita | CWE-20 | Documentato |
| AUTH-01 ÷ AUTH-06 | Missing Authorization | CWE-862 | Risolto |
| AUTH-07 | Matching permissivo | CWE-20 | Risolto |
| BL-01, BL-02 | Accesso non autorizzato | CWE-862 | Risolto |
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive| CWE-178 | Risolto |
| SEC-DAO-02 | Numero carta/CVV in chiaro| CWE-312 | Documentato |
| SEC-DAO-03 | Doppio hashing nel setter `Utente.setPassword()` | CWE-1064 | Documentato |
| — | SHA-1 per password | CWE-916 | Accettato |
| — | CSRF assente | CWE-352 | Accettato |
| — | Brute-force protection assente | CWE-307 | Accettato |
| — | Session fixation | CWE-384 | Accettato |

### 8.4 Nota sul conteggio

Il totale comprende le esecuzioni multiple dei test parametrizzati (`@ParameterizedTest`) in `HomeServletAmministratoreTest`, `CercaProdottoPerModificaServletTest`, `RendiAmministratoreServletTest`, `AggiuntaProdottoServletTest`, `InizioServletTest` e `ModificaProdottiServletAmministratoreTest`.

### 8.5 Nota metodologica finale

La suite adotta **tre strategie complementari**, ciascuna adatta al livello di astrazione del componente testato:

1. **Test diretti** per i Bean (nessuna infrastruttura)
2. **Mockito** per i Servlet (simulazione del web container)
3. **H2 in-memory** per i DAO (esecuzione SQL reale)

A queste si aggiungono l'**analisi statica mirata** (per documentare i pattern di sicurezza) e **SonarQube** (per la verifica statica sull'intero codebase). Ogni strategia copre aspetti diversi e **non sono ridondanti**: il valore aggiunto è dimostrato dal finding SEC-DAO-01, scoperto solo grazie ai test funzionali H2.