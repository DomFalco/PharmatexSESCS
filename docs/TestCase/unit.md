# Test Unitari e Analisi Statica — Suite OWASP

**Strategia:** [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)  
**Framework:** JUnit 5 + Mockito 4.11.0 + AssertJ 3.27.7  
**Test totali (questa categoria):** 305 (29 classi, 5 aree OWASP)  
**Data ultima esecuzione:** 07/10/2026

---

## Indice

- [1. Introduzione](#1-introduzione)
    - [1.1 Strategia e organizzazione](#11-strategia-e-organizzazione)
    - [1.2 Struttura delle directory dei test](#12-struttura-delle-directory-dei-test)
    - [1.3 Stack di test](#13-stack-di-test)
- [2. Pattern di test utilizzati](#2-pattern-di-test-utilizzati)
    - [2.1 Test delle Servlet (Mockito)](#21-test-delle-servlet-mockito)
    - [2.2 Test delle classi Model](#22-test-delle-classi-model)
    - [2.3 Test che toccano il DB](#23-test-che-toccano-il-db)
    - [2.4 Test di "documentazione"](#24-test-di-documentazione)
    - [2.5 Analisi statica del sorgente per i DAO](#25-analisi-statica-del-sorgente-per-i-dao)
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
    - [7.1 Analisi statica del sorgente — 49 test](#71-analisi-statica-del-sorgente--49-test)
    - [7.2 Miglioramenti implementati](#72-miglioramenti-implementati)
    - [7.3 Finding documentati](#73-finding-documentati)
- [8. Dettaglio vulnerabilità critiche](#8-dettaglio-vulnerabilità-critiche)
    - [8.1 NPE su `mat.equalsIgnoreCase()` in MaterialeServlet (SEC-IV-01)](#81-npe-su-matequalsignorecase-in-materialeservlet-sec-iv-01)
    - [8.2 Doppio forward in MaterialeServlet (SEC-IV-02)](#82-doppio-forward-in-materialeservlet-sec-iv-02)
    - [8.3 Sanitizzazione input assente in FiltraggioServletMateriale (IV-01)](#83-sanitizzazione-input-assente-in-filtraggioservletmateriale-iv-01)
    - [8.4 Validazione regex assente in InizioServlet (IV-02)](#84-validazione-regex-assente-in-inizioservlet-iv-02)
    - [8.5 Missing Authorization in 6 Servlet (AUTH-01÷06)](#85-missing-authorization-in-6-servlet-auth-0106)
    - [8.6 Matching permissivo in RendiAmministratoreServlet (AUTH-07)](#86-matching-permissivo-in-rendiamministratoreservlet-auth-07)
    - [8.7 Accesso non autorizzato in HomeServlet e DatiPagamentoServlet (BL-01, BL-02)](#87-accesso-non-autorizzato-in-homeservlet-e-datipagamentoservlet-bl-01-bl-02)
    - [8.8 Riepilogo vulnerabilità critiche](#88-riepilogo-vulnerabilità-critiche)
- [9. Riepilogo](#9-riepilogo)
    - [9.1 Metriche di questa categoria](#91-metriche-di-questa-categoria)
    - [9.2 Copertura per Area OWASP](#92-copertura-per-area-owasp)
    - [9.3 Finding principali](#93-finding-principali)
    - [9.4 Nota sul conteggio](#94-nota-sul-conteggio)
    - [9.5 Nota metodologica](#95-nota-metodologica)

---

## 1. Introduzione

Questo report documenta la suite di **test unitari e di analisi statica** del progetto: **305 test** distribuiti su **29 classi** e organizzati in **5 aree** secondo l'OWASP Testing Guide. I **test funzionali H2** (176 test) sono documentati separatamente in [`functional.md`](functional.md).

Per la panoramica complessiva della strategia di test (perché due categorie, valore aggiunto, finding aggregati) vedi [`security-test.md`](security-test.md).

### 1.1 Strategia e organizzazione

I test sono organizzati in **5 aree OWASP**, ciascuna mappata sulle categorie dell'**OWASP Top 10 (2021)**:

| Area OWASP | OWASP Top 10 (2021) | Focus | Test |
|------------|:-------------------:|-------|:----:|
| **Data Protection** | A02:2021 — Cryptographic Failures | Hashing, credenziali, dati sensibili | 20 |
| **Input Validation** | A03:2021 — Injection | Sanitizzazione, XSS, SQL Injection | 83 |
| **Authorization & Access Control** | A01:2021 — Broken Access Control | Autenticazione, sessioni, privilegi | 75 |
| **Business Logic** | A04:2021 — Insecure Design | Logica applicativa, flussi, calcoli | 78 |
| **DAO Security** | A03:2021 — Injection | Query SQL, PreparedStatement, pattern statici | 49 |
| **TOTALE** | | | **305** |

### 1.2 Struttura delle directory dei test

```
src/test/java/
└── security/
    └── unit/                          (29 file — test unitari)
        ├── authorization/             (7 file)
        ├── businesslogic/             (6 file)
        ├── daointegration/            (4 file — analisi statica)
        ├── dataprotection/            (3 file)
        └── inputvalidation/           (9 file)
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

**Nota:** JUnit e Mockito sono compatibili con Java 8 (source/target del progetto).

---

## 2. Pattern di test utilizzati

### 2.1 Test delle Servlet (Mockito)

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

**Verifiche:**

- Uso di `PreparedStatement` in tutti i metodi
- Assenza di concatenazione di stringhe nelle query (protezione SQL Injection)
- Presenza di `Statement.RETURN_GENERATED_KEYS`
- Uso di `try-with-resources` per `Connection`, `PreparedStatement`, `ResultSet`
- Assenza di `SELECT *`

**Perché l'analisi statica del sorgente per i DAO:**

I DAO del progetto non sono testabili con il DB reale nell'ambiente JUnit, per tre motivi:

1. **Il DB è dentro Docker**: nei test unitari il container MySQL non è raggiungibile tramite l'host `db` (esiste solo nella rete Docker Compose).
2. **`ConPool` applica il fail-fast**: se `MYSQL_PASSWORD` non è impostata, `getConnection()` lancia `IllegalStateException`.
3. **I metodi dei DAO sono `static`**: Mockito 4.11.0 (l'unica versione compatibile con Java 8) non può mockare metodi statici.

Questa strategia copre esattamente ciò che Snyk Code e SonarCloud non rilevano per i DAO:

| Verifica | Rilevanza |
|----------|-----------|
| Uso di `PreparedStatement` in tutti i metodi | Protezione da SQL Injection |
| Assenza di concatenazione di stringhe nelle query | Anti-pattern noto |
| Uso di `Statement.RETURN_GENERATED_KEYS` | Corretto recupero degli ID |
| Uso di `try-with-resources` per la `Connection` | Prevenzione di resource leak |
| Assenza di `SELECT *` | Fragilità dello schema |
| Firma dei metodi (static, return type) | Coerenza dell'interfaccia |

> **Nota:** il comportamento a runtime dei DAO (query reali, INSERT/SELECT/UPDATE, SQL Injection neutralizzata) è verificato dai **test funzionali H2**, documentati in [`functional.md`](functional.md).

---

## 3. Data Protection — 20 test (A02:2021)

> **OWASP Top 10 (2021):** [A02:2021 — Cryptographic Failures](https://owasp.org/Top10/A02_2021-Cryptographic_Failures/)

**Obiettivo OWASP:** verificare che i dati sensibili (password, credenziali) siano protetti correttamente e che non ci siano esposizioni accidentali.

### 3.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità |
|--------|:----:|-------------------------|
| `Utente.java` | 8 | `setPassword()` / `getPassword()` |
| `ConPool.java` | 7 | `getConnection()` (connessione DB) |
| `ServletErrorHelper.java` | 5 | `sendError()` (gestione errori HTTP) |

**File di test:**
- `PasswordHashingTest.java`
- `ConPoolTest.java`
- `ServletErrorHelperTest.java`

### 3.2 Miglioramenti implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Hashing password** | SHA-1 senza salt | Documentato come rischio accettato (CWE-916) | Consapevolezza della vulnerabilità |
| **Credenziali DB** | Password hardcoded in `ConPool.java` | Lette da variabili d'ambiente (`System.getenv()`) | Fix GitGuardian |
| **Fail-fast** | Nessun controllo | `IllegalStateException` se `MYSQL_PASSWORD` non impostata | Prevenzione di avvio con configurazione incompleta |
| **Error handling** | `System.err.println` | `java.util.logging.Logger` con logging sicuro | No stack trace esposti |

### 3.3 Vulnerabilità documentate

- **CWE-916** ([Use of Password Hash With Insufficient Computational Effort](https://cwe.mitre.org/data/definitions/916.html)): SHA-1 senza salt per l'hashing delle password in `Utente.java`. **Rischio accettato** [`snyk.md`](https://github.com/DomFalco/PharmatexSESCS/blob/master/docs/Snyk/README.md).
- **Fix GitGuardian**: credenziali DB lette da variabili d'ambiente con fail-fast se `MYSQL_PASSWORD` non è impostata.

---

## 4. Input Validation — 83 test (A03:2021)

> **OWASP Top 10 (2021):** [A03:2021 — Injection](https://owasp.org/Top10/A03_2021-Injection/)

**Obiettivo OWASP:** verificare che gli input provenienti dall'utente siano validati e sanitizzati prima dell'uso, prevenendo XSS, SQL Injection e NPE.

### 4.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità | Fix applicato |
|--------|:----:|-------------------------|---------------|
| `FiltraggioServletMateriale.java` | 9 | `/FiltraggioServletMateriale?prodotto=...&materiale=...` | Sanitizzazione `replaceAll` |
| `InizioServlet.java` | 9 | `/InizioServlet?action=...&valore=...` | Validazione regex |
| `RicercaServlet.java` | 6 | `/RicercaServlet?search=...` | Documenta NPE su input null |
| `FiltraggioServletPrezzo.java` | 8 | `/FiltraggioServletPrezzo?prezzomin=...&prezzomax=...` | Documenta NumberFormatException |
| `CarrelloServlet.java` | 9 | `/CarrelloServlet?action=...&quantita=...` | Documenta NPE e doppio forward |
| `AggiuntaProdottoServlet.java` | 11 | `/AggiuntaProdottoServlet` (17 parametri) | Documenta validazione assente |
| `ModificaProdottiServletAmministratore.java` | 11 | `/ModificaProdottiServletAmministratore?nuovoPrezzo=...&quantitaTotale=...` | Documenta NPE e validazione assente |
| `MaterialeServlet.java` | 9 | `/MaterialeServlet` (filtro materiale) | **Fix NPE + doppio forward** |
| `JspHelper.java` | 9 | Estrazione dati per JSP | Estrazione sicura dati dalla request |

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
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` quando `mat == null` | CWE-476 | `/MaterialeServlet` | `testMatNullNonCausaNPE` | Aggiunto null check `if (mat == null) mat = ""` |
| SEC-IV-02 | Doppio forward quando `prodottiMateriale.isEmpty()` | CWE-754 | `/MaterialeServlet` | `testNessunDoppioForward` | Aggiunto `return` dopo il primo forward |
| IV-01 | Sanitizzazione input assente | CWE-79 | `/FiltraggioServletMateriale` | `testSanitizationBeforeComparison` | Aggiunto `replaceAll("[^a-zA-Z0-9\\s]", "")` |
| IV-02 | Validazione regex assente | CWE-20 | `/InizioServlet` | `testActionConScriptRestituisce400` | Aggiunto `matches("[a-zA-Z0-9\\s]+")` |
| IV-03 | NPE su input null | CWE-476 | `/RicercaServlet` | `testInputNullCausaNPE` | Documentato (fix richiede null check) |
| IV-04 | NumberFormatException non gestita | CWE-20 | `/FiltraggioServletPrezzo` | `testPrezzoNonNumericoCausaNumberFormatException` | Documentato |

---

## 5. Authorization & Access Control — 75 test (A01:2021)

> **OWASP Top 10 (2021):** [A01:2021 — Broken Access Control](https://owasp.org/Top10/A01_2021-Broken_Access_Control/)

**Obiettivo OWASP:** verificare che l'autenticazione e l'autorizzazione siano applicate correttamente, prevenendo accessi non autorizzati.

### 5.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint | Fix applicato |
|--------|:----:|----------|---------------|
| `LoginServlet.java` | 11 | `/LoginServlet` (login, logout, carrello, riepilogo) | Documenta CSRF, brute-force, session fixation |
| `RegistrazioneServlet.java` | 13 | `/RegistrazioneServlet` (12 parametri) | Documenta validazione assente |
| `HomeServletAmministratore.java` | 11 | `/HomeServletAmministratore?valore=...` | **CWE-862 (Broken Access Control)** |
| `RendiAmministratoreServlet.java` | 12 | `/RendiAmministratoreServlet?action=...` | **CWE-862 + CWE-20** |
| `PagamentoServlet.java` | 6 | `/PagamentoServlet` | **CWE-862** |
| `CercaProdottoPerModificaServlet.java` | 10 | `/CercaProdottoPerModificaServlet?search=...` | **CWE-862 + CWE-20** |
| `RimuoviProdottoServlet.java` | 10 | `/RimuoviProdottoServlet` | **CWE-862** |

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
| AUTH-01 | Accesso admin senza controllo | CWE-862 | `/HomeServletAmministratore` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check `session.getAttribute("Amministratore")` |
| AUTH-02 | Privilege escalation via `?action=amministratore` | CWE-862 | `/RendiAmministratoreServlet` | `testAnonimoNonPuoPromuovereAdmin` | Aggiunto check autorizzazione + `startsWith` |
| AUTH-03 | Pagamento accessibile senza login | CWE-862 | `/PagamentoServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check `session.getAttribute("Utente")` |
| AUTH-04 | Accesso admin via `?valore=home` in HomeServlet | CWE-862 | `/HomePage?valore=home` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check autorizzazione |
| AUTH-05 | Ricerca prodotto admin senza controllo | CWE-862 | `/CercaProdottoPerModificaServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check admin + validazione `search` |
| AUTH-06 | Cancellazione prodotto senza controllo | CWE-862 | `/RimuoviProdottoServlet` | `testAnonimoSessioneNullRiceve403` | Aggiunto check admin + null check prodotto |
| AUTH-07 | Matching permissivo `contains` invece di `startsWith` | CWE-20 | `/RendiAmministratoreServlet` | `testMatchingPermissivoBloccato` | Sostituito `contains` con `startsWith` |

---

## 6. Business Logic — 78 test (A04:2021)

> **OWASP Top 10 (2021):** [A04:2021 — Insecure Design](https://owasp.org/Top10/A04_2021-Insecure_Design/)

**Obiettivo OWASP:** verificare che la logica applicativa (calcoli, flussi, validazioni di dominio) sia corretta e non manipolabile.

### 6.1 Endpoint e funzionalità testate

| Classe | Test | Endpoint / Funzionalità | Fix applicato |
|--------|:----:|-------------------------|---------------|
| `Prodotto.java` | 18 | Getter/setter, validazione | Documenta validazione assente |
| `AcquistoProdotti.java` | 18 | Getter/setter, mutazione condivisa | Documenta mutazione condivisa |
| `Carta.java` | 17 | Getter/setter, dati carta | Documenta violazione PCI DSS |
| `DatiPagamentoServlet.java` | 10 | `/DatiPagamentoServlet` (pagamento) | **CWE-862** |
| `HomeServlet.java` | 10 | `/HomePage?valore=home` | **CWE-862** |
| `Registrazione.java` | 5 | `/Registrazione` (form) | Servlet di sola presentazione |

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
| BL-01 | Accesso admin via `?valore=home` senza controllo | CWE-862 | `/HomePage?valore=home` | `testAnonimoConValoreHomeRiceve403` | Aggiunto check autorizzazione |
| BL-02 | Pagamento accessibile senza login/carrello vuoto | CWE-862 | `/DatiPagamentoServlet` | `testAnonimoSessioneNullRiceve403` | Controllo autenticazione + carrello |

---

## 7. DAO Security — Analisi statica (A03:2021)

> **OWASP Top 10 (2021):** [A03:2021 — Injection](https://owasp.org/Top10/A03_2021-Injection/)

**Obiettivo OWASP:** verificare che le query SQL siano protette da SQL Injection tramite **analisi statica del sorgente** (patterns di sicurezza testuali).

> **Nota:** i test funzionali H2 dei DAO (38 test) e delle Servlet (136 test) sono documentati in [`functional.md`](functional.md).

### 7.1 Analisi statica del sorgente — 49 test

**File di test:** `src/test/java/security/unit/daointegration/`

| Classe | Test | Query / Operazioni | Note |
|--------|:----:|---------------------|------|
| `ProdottoDAO.java` | 10 | `SELECT`, `INSERT`, `UPDATE`, `DELETE` | Usa PreparedStatement, no SQL Injection |
| `UtenteDAO.java` | 14 | `SELECT`, `INSERT`, `UPDATE` (login, registrazione) | Documenta SHA1, verifica assenza `SELECT *` |
| `AcquistoProdottiDAO.java` | 13 | `SELECT`, `INSERT` (acquisti) | Non estende HttpServlet (buona pratica) |
| `CartaDAO.java` | 12 | `SELECT`, `INSERT` (carte) | Documenta SELECT senza WHERE, CVV in chiaro |

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
| SEC-DAO-02 | Numero carta e CVV salvati in chiaro nella tabella `CartaDiCredito`. Il CVV non dovrebbe **mai** essere persistito (PCI DSS 3.2) | CWE-312 | `CartaDAO` | Documentato |
| SEC-DAO-03 | Il setter `Utente.setPassword()` applica SHA-1: ogni lettura dal DB produce un doppio hash `SHA1(SHA1(pwd))` | CWE-1064 | `Utente.java` | Documentato |
| SEC-DAO-04 | `UtenteDAO.doLogin` usa `SHA1()` per confrontare la password nel database | CWE-916 | `UtenteDAO` | Documentato |
| SEC-DAO-05 | `CartaDAO.aggiuntaCredenzialiPagamento` esegue `SELECT numeroCarta FROM CartaDiCredito` senza WHERE, caricando tutte le carte in memoria | CWE-770 | `CartaDAO` | Documentato |
| SEC-DAO-06 | I DAO `ProdottoDAO` e `UtenteDAO` estendono `HttpServlet` (design smell: un DAO non è una Servlet) | — | `ProdottoDAO`, `UtenteDAO` | Documentato |
| SEC-DAO-07 | Eccezioni SQL wrappate in `DataAccessException` custom (miglioramento rispetto a `RuntimeException` generica) | CWE-391 | Tutti i DAO | Risolto |

**Nota su SEC-DAO-03:** il doppio hashing non compromette il login (perché `doLogin` confronta l'hash nel DB con `SHA1(input)` prima del re-hashing del bean), ma è un **design smell** che viola il principio di separazione tra Model e hashing. Un setter non dovrebbe mai applicare trasformazioni crittografiche.

**Nota:** il trattino `—` nella colonna CWE indica un **design smell** o problema architetturale che non mappa su un CWE specifico (non è una vulnerabilità di sicurezza ma una violazione di best practice).

> **Nota:** il finding **SEC-DAO-01** (`doRetriveBySearch` non case-insensitive) è stato scoperto **solo** grazie ai test funzionali H2, ed è documentato in [`functional.md`](functional.md).

---

## 8. Dettaglio vulnerabilità critiche

Questa sezione documenta in dettaglio le **vulnerabilità critiche risolte** emerse dall'analisi con test unitari e statici. Per ogni vulnerabilità sono descritti: contesto, scenario osservato e patch applicata.

### 8.1 NPE su `mat.equalsIgnoreCase()` in MaterialeServlet (SEC-IV-01)

- **File:** `src/main/java/Controller/MaterialeServlet.java`
- **Test:** `testMatNullNonCausaNPE` (`MaterialeServletTest`)
- **CWE:** [CWE-476](https://cwe.mitre.org/data/definitions/476.html) (NULL Pointer Dereference)
- **Severità:** Critica

**Vulnerabilità**

Il Servlet recuperava il parametro `prodotto` dalla request e lo confrontava direttamente con `equalsIgnoreCase()` senza verificare che non fosse `null`. Se l'utente accedeva a `/MaterialeServlet` senza inviare il parametro, il server rispondeva con `NullPointerException` → HTTP 500, esponendo inoltre lo stack trace nei log applicativi.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Utente invia `?prodotto=Materasso` | Elaborazione corretta |
| Utente accede senza parametro `prodotto` | **NPE → HTTP 500** |
| Con patch applicata | Null check → valore vuoto → risposta 400 |

**Patch applicata**

Aggiunto un null check esplicito prima del confronto: se `mat` è `null`, viene impostato a stringa vuota. Il Servlet prosegue con l'elaborazione normale senza sollevare eccezioni.

---

### 8.2 Doppio forward in MaterialeServlet (SEC-IV-02)

- **File:** `src/main/java/Controller/MaterialeServlet.java`
- **Test:** `testNessunDoppioForward` (`MaterialeServletTest`)
- **CWE:** [CWE-754](https://cwe.mitre.org/data/definitions/754.html) (Improper Check for Unusual or Exceptional Conditions)
- **Severità:** Critica

**Vulnerabilità**

Nel ramo in cui la lista `prodottiMateriale` risultava vuota, il Servlet eseguiva un `forward()` verso una JSP di errore e successivamente, **senza un `return`**, continuava l'esecuzione chiamando un secondo `forward()` verso la pagina principale. In un container reale (Tomcat) questo comportamento genera `IllegalStateException: Cannot forward after response has been committed`.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Lista prodotti piena | Singolo forward → OK |
| Lista prodotti vuota | Doppio forward → **IllegalStateException** |
| Con patch applicata | `return` dopo il primo forward → OK |

**Patch applicata**

Aggiunto `return;` immediatamente dopo il primo `forward()` nel ramo "lista vuota". Il secondo forward non viene più raggiunto.

---

### 8.3 Sanitizzazione input assente in FiltraggioServletMateriale (IV-01)

- **File:** `src/main/java/Controller/FiltraggioServletMateriale.java`
- **Test:** `testSanitizationBeforeComparison` (`FiltraggioServletMaterialeTest`)
- **CWE:** [CWE-79](https://cwe.mitre.org/data/definitions/79.html) (Improper Neutralization of Input During Web Page Generation — XSS)
- **Severità:** Alta

**Vulnerabilità**

Il parametro `prodotto` veniva confrontato con `equalsIgnoreCase()` senza sanitizzazione preventiva e successivamente memorizzato in sessione. Un payload contenente markup HTML/JavaScript (es. `<script>alert(1)</script>`) poteva raggiungere la JSP che lo rendeva senza escape, esponendo l'utente a XSS riflesso.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Parametro `prodotto=Materasso` | OK |
| Parametro `prodotto=<script>alert(1)</script>` | Payload propagato → **XSS** |
| Con patch applicata | Payload sanitizzato prima del confronto |

**Patch applicata**

Aggiunta sanitizzazione con `replaceAll("[^a-zA-Z0-9\\s]", "")` sul valore di `mat` e `materiale`, subito dopo il recupero dei parametri e prima di ogni confronto/memorizzazione.

---

### 8.4 Validazione regex assente in InizioServlet (IV-02)

- **File:** `src/main/java/Controller/InizioServlet.java`
- **Test:** `testActionConScriptRestituisce400` (`InizioServletTest`)
- **CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html) (Improper Input Validation)
- **Severità:** Alta

**Vulnerabilità**

Il parametro `action` veniva usato direttamente per instradare il flusso applicativo senza validazione formale. Un attaccante poteva inviare valori arbitrari (inclusi payload di test XSS o input malformati) che venivano elaborati come se fossero azioni legittime.

**Scenario**

| Scenario | Comportamento |
|---|---|
| `action=home` | OK |
| `action=<script>...</script>` | **Payload accettato** |
| Con patch applicata | Validazione regex → HTTP 400 |

**Patch applicata**

Aggiunta validazione con `matches("[a-zA-Z0-9\\s]+")` sul parametro `action`. Se il valore non rispetta il pattern consentito, il Servlet risponde con `sendError(400)`.

---

### 8.5 Missing Authorization in 6 Servlet (AUTH-01÷06)

- **File:**
    - `HomeServletAmministratore.java`
    - `RendiAmministratoreServlet.java`
    - `PagamentoServlet.java`
    - `HomeServlet.java` (`?valore=home`)
    - `CercaProdottoPerModificaServlet.java`
    - `RimuoviProdottoServlet.java`
- **Test:** vari (`testAnonimoConValoreHomeRiceve403`, `testAnonimoNonPuoPromuovereAdmin`, `testAnonimoSessioneNullRiceve403`, ecc.)
- **CWE:** [CWE-862](https://cwe.mitre.org/data/definitions/862.html) (Missing Authorization)
- **Severità:** Critica

**Vulnerabilità**

Sei Servlet che erogano funzionalità amministrative o sensibili non verificavano lo stato di autenticazione/autorizzazione dell'utente. Chiunque poteva accedere via URL diretta a pagine di amministrazione, promuoversi ad amministratore, effettuare pagamenti senza login o cancellare prodotti.

**Scenario**

| Scenario | Comportamento prima | Comportamento dopo |
|---|---|---|
| Anonimo accede a `/HomeServletAmministratore` | Pagina caricata | **HTTP 403** |
| Anonimo chiama `/RendiAmministratoreServlet?action=amministratore` | Si promuove admin | **HTTP 403** |
| Anonimo accede a `/PagamentoServlet` | Pagina caricata | **HTTP 403** |
| Anonimo accede a `/HomePage?valore=home` | Vista admin | **HTTP 403** |
| Anonimo accede a `/CercaProdottoPerModificaServlet` | Accesso consentito | **HTTP 403** |
| Anonimo chiama `/RimuoviProdottoServlet` | Cancellazione consentita | **HTTP 403** |

**Patch applicata**

Aggiunto in ogni Servlet il controllo `session.getAttribute("Utente")` / `session.getAttribute("Amministratore")` con verifica di `isAmministratore()`. Se l'utente non è autenticato/autorizzato, il Servlet risponde con `sendError(403)`.

---

### 8.6 Matching permissivo in RendiAmministratoreServlet (AUTH-07)

- **File:** `src/main/java/Controller/RendiAmministratoreServlet.java`
- **Test:** `testMatchingPermissivoBloccato` (`RendiAmministratoreServletTest`)
- **CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html) (Improper Input Validation)
- **Severità:** Alta

**Vulnerabilità**

La Servlet usava `contains("amministratore")` per riconoscere le azioni legittime. Questo consentiva il match di valori come `notamministratore`, `amministratoreHack` o `xamministratore`, aggirando i controlli previsti.

**Scenario**

| Scenario | Comportamento |
|---|---|
| `action=amministratore` | OK |
| `action=notamministratore` | **Accettato (falso positivo)** |
| Con patch applicata | Rifiutato |

**Patch applicata**

Sostituito `contains(...)` con `startsWith("amministratore")` per richiedere che la stringa inizi esattamente con il prefisso previsto.

---

### 8.7 Accesso non autorizzato in HomeServlet e DatiPagamentoServlet (BL-01, BL-02)

- **File:**
    - `HomeServlet.java`
    - `DatiPagamentoServlet.java`
- **Test:** `testAnonimoConValoreHomeRiceve403` (`HomeServletTest`), `testAnonimoSessioneNullRiceve403` (`DatiPagamentoServletTest`)
- **CWE:** [CWE-862](https://cwe.mitre.org/data/definitions/862.html) (Missing Authorization)
- **Severità:** Critica

**Vulnerabilità**

- **HomeServlet**: il ramo attivato da `?valore=home` esponeva una vista amministrativa a utenti non autenticati.
- **DatiPagamentoServlet**: consentiva l'avvio del flusso di pagamento senza verificare né l'autenticazione dell'utente né la presenza di articoli nel carrello, esponendo il sistema a utilizzi impropri.

**Scenario**

| Scenario | Comportamento prima | Comportamento dopo |
|---|---|---|
| Anonimo accede a `/HomePage?valore=home` | Vista admin | **HTTP 403** |
| Anonimo accede a `/DatiPagamentoServlet` | Pagamento avviato | **HTTP 403** |
| Utente loggato con carrello vuoto accede a `/DatiPagamentoServlet` | Pagamento avviato | Rifiutato |

**Patch applicata**

- `HomeServlet`: aggiunto check `session.getAttribute("Amministratore")` + `isAmministratore()`.
- `DatiPagamentoServlet`: aggiunto check `session.getAttribute("Utente")` e verifica che il carrello non sia `null` o vuoto prima di procedere.

---

### 8.8 Riepilogo vulnerabilità critiche

| ID | Vulnerabilità | CWE | Severità | Stato |
|----|---------------|:---:|:--------:|:-----:|
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` | CWE-476 | Critica | Risolto |
| SEC-IV-02 | Doppio forward in `MaterialeServlet` | CWE-754 | Critica | Risolto |
| IV-01 | Sanitizzazione input assente | CWE-79 | Alta | Risolto |
| IV-02 | Validazione regex assente | CWE-20 | Alta | Risolto |
| AUTH-01÷06 | Missing Authorization | CWE-862 | Critica | Risolto |
| AUTH-07 | Matching permissivo `contains` | CWE-20 | Alta | Risolto |
| BL-01, BL-02 | Accesso non autorizzato | CWE-862 | Critica | Risolto |
| SEC-DAO-07 | Eccezioni SQL wrappate in `DataAccessException` | CWE-391 | Media | Risolto |

Le vulnerabilità **documentate come rischio accettato** (SHA-1, CSRF, session fixation, violazioni PCI DSS) sono trattate nella sezione 3.3 e 7.3.

---

## 9. Riepilogo

### 9.1 Metriche di questa categoria

| Metrica | Valore |
|:---|:---:|
| **Test unitari totali** | **305** |
| **Classi di test** | **29** |
| **Aree OWASP** | **5** |
| **Success rate** | **100%** |

### 9.2 Copertura per Area OWASP

| Area OWASP | OWASP Top 10 (2021) | Classi | Test |
|------------|:-------------------:|:------:|:----:|
| Data Protection | A02:2021 — Cryptographic Failures | 3 | 20 |
| Input Validation | A03:2021 — Injection | 9 | 83 |
| Authorization | A01:2021 — Broken Access Control | 7 | 75 |
| Business Logic | A04:2021 — Insecure Design | 6 | 78 |
| DAO Security — statici | A03:2021 — Injection | 4 | 49 |
| **TOTALE** | | **29** | **305** |

### 9.3 Finding principali

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
| SEC-DAO-02 | Numero carta/CVV in chiaro | CWE-312 | Documentato |
| SEC-DAO-03 | Doppio hashing nel setter `Utente.setPassword()` | CWE-1064 | Documentato |
| SEC-DAO-04 | `doLogin` usa `SHA1()` | CWE-916 | Documentato |
| SEC-DAO-05 | `CartaDAO` SELECT senza WHERE | CWE-770 | Documentato |
| SEC-DAO-06 | DAO estendono `HttpServlet` (design smell) | — | Documentato |
| SEC-DAO-07 | Eccezioni SQL wrappate in `DataAccessException` | CWE-391 | Risolto |
| ACC-01 | SHA-1 per password (rischio accettato) | CWE-916 | **Accettato** |
| ACC-02 | CSRF assente (rischio accettato) | CWE-352 | **Accettato** |
| ACC-03 | Brute-force protection assente (rischio accettato) | CWE-307 | **Accettato** |
| ACC-04 | Session fixation (rischio accettato) | CWE-384 | **Accettato** |

### 9.4 Nota sul conteggio

Il totale comprende le esecuzioni multiple dei test parametrizzati (`@ParameterizedTest`) in `HomeServletAmministratoreTest`, `CercaProdottoPerModificaServletTest`, `RendiAmministratoreServletTest`, `AggiuntaProdottoServletTest`, `InizioServletTest` e `ModificaProdottiServletAmministratoreTest`.

Il totale include inoltre il test `testNessunSelectStar` aggiunto in `UtenteDAOTest`.

### 9.5 Nota metodologica

Questo report copre **tre strategie di verifica**:

1. **Test diretti** per i Bean (nessuna infrastruttura)
2. **Mockito** per le Servlet (simulazione del web container)
3. **Analisi statica** del sorgente per i DAO

Per la strategia complessiva (compresi i **test funzionali H2**) e le metriche aggregate del progetto, vedi [`security-test.md`](security-test.md). Per i dettagli sui test funzionali H2, vedi [`functional.md`](functional.md).