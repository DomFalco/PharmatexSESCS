# Test Funzionali — Suite H2 in-memory

**Strategia:** [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)  
**Framework:** JUnit 5 + Mockito 4.11.0 + AssertJ 3.27.7 + H2 2.2.224  
**Test funzionali totali (questa categoria):** 176 (21 classi, 16 Servlet + 4 DAO + 1 Eccezione)  
**Data ultima esecuzione:** 07/10/2026

---

## Indice

- [1. Introduzione](#1-introduzione)
    - [1.1 Perché i test funzionali](#11-perché-i-test-funzionali)
    - [1.2 Cosa coprono i test funzionali](#12-cosa-coprono-i-test-funzionali)
    - [1.3 Cosa NON coprono i test funzionali](#13-cosa-non-coprono-i-test-funzionali)
- [2. Infrastruttura H2](#2-infrastruttura-h2)
    - [2.1 Architettura](#21-architettura)
    - [2.2 File di supporto](#22-file-di-supporto)
    - [2.3 Modifiche al codice di produzione](#23-modifiche-al-codice-di-produzione)
    - [2.4 Registrazione della funzione SHA1](#24-registrazione-della-funzione-sha1)
- [3. Test funzionali DAO — 38 test](#3-test-funzionali-dao--38-test)
    - [3.1 ProdottoDAOH2Test (14 test)](#31-prodottodaoh2test-14-test)
    - [3.2 UtenteDAOH2Test (13 test)](#32-utentedaoh2test-13-test)
    - [3.3 CartaDAOH2Test (4 test)](#33-cartadaoh2test-4-test)
    - [3.4 AcquistoProdottiDAOH2Test (7 test)](#34-acquistoprodottidaoh2test-7-test)
    - [3.5 DataAccessExceptionTest (2 test)](#35-dataaccessexceptiontest-2-test)
- [4. Test funzionali Servlet — 136 test](#4-test-funzionali-servlet--136-test)
    - [4.1 Authorization — 54 test](#41-authorization--54-test)
    - [4.2 Business Logic — 19 test](#42-business-logic--19-test)
    - [4.3 Input Validation — 63 test](#43-input-validation--63-test)
- [5. Finding scoperti](#5-finding-scoperti)
    - [5.1 SEC-DAO-01 — doRetriveBySearch non case-insensitive](#51-sec-dao-01--doretrivebysearch-non-case-insensitive)
    - [5.2 SEC-IV-03 — Doppio forward in FiltraggioServletPrezzo](#52-sec-iv-03--doppio-forward-in-filtraggioservletprezzo)
    - [5.3 Altri finding documentati](#53-altri-finding-documentati)
- [6. Metriche finali](#6-metriche-finali)
    - [6.1 Test funzionali per area](#61-test-funzionali-per-area)
    - [6.2 Metriche di questa categoria](#62-metriche-di-questa-categoria)
    - [6.3 Finding scoperti da questa categoria](#63-finding-scoperti-da-questa-categoria)
- [7. Dettaglio vulnerabilità critiche](#7-dettaglio-vulnerabilità-critiche)
    - [7.1 SEC-DAO-01 — doRetriveBySearch non case-insensitive](#71-sec-dao-01--doretrivebysearch-non-case-insensitive)
    - [7.2 SEC-IV-03 — Doppio forward in FiltraggioServletPrezzo](#72-sec-iv-03--doppio-forward-in-filtraggioservletprezzo)
    - [7.3 FUNC-AUTH-01 — Missing Authorization in 2 Servlet](#73-func-auth-01--missing-authorization-in-2-servlet)
    - [7.4 FUNC-IV-01 — NPE su parametri null in 3 Servlet](#74-func-iv-01--npe-su-parametri-null-in-3-servlet)
    - [7.5 FUNC-IV-02 — NumberFormatException su input non numerici](#75-func-iv-02--numberformatexception-su-input-non-numerici)
    - [7.6 FUNC-IV-03 — Silent drop in ModificaProdottiServletAmministratore](#76-func-iv-03--silent-drop-in-modificaprodottiservletamministratore)
    - [7.7 FUNC-BL-01 — IndexOutOfBoundsException in DatiPagamentoServlet](#77-func-bl-01--indexoutofboundsexception-in-datipagamentoservlet)
    - [7.8 FUNC-BL-02 — Doppio forward in RimuoviProdottoServlet](#78-func-bl-02--doppio-forward-in-rimuoviprodottoservlet)
    - [7.9 Riepilogo vulnerabilità critiche](#79-riepilogo-vulnerabilità-critiche)
- [8. Nota metodologica](#8-nota-metodologica)
    - [8.1 Perché H2 in-memory](#81-perché-h2-in-memory)
    - [8.2 Limiti dell'approccio](#82-limiti-dellapproccio)

---

## 1. Introduzione

I **test funzionali** sono test che **eseguono il codice di produzione** contro un database **H2 in-memory** configurato in modalità MySQL. Permettono di verificare il comportamento a runtime che i test unitari (basati su Mockito) non riescono a coprire: query SQL, INSERT/SELECT/UPDATE/DELETE, gestione delle eccezioni DB, e interazione tra Servlet e DAO.

Per la panoramica complessiva della strategia di test (perché due categorie, valore aggiunto, finding aggregati) vedi [`security-test.md`](security-test.md).

### 1.1 Perché i test funzionali

I test unitari con Mockito verificano:
- Il flusso logico delle Servlet (autorizzazione, redirect, forward)
- Il comportamento dei Bean (getter/setter, validazione)
- I pattern di sicurezza (analisi statica del sorgente)

**Ma non verificano:**
- Che una query SQL funzioni davvero
- Che un INSERT generi l'ID corretto
- Che un payload di SQL Injection venga neutralizzato a runtime
- Che il flusso Servlet → DAO → DB funzioni end-to-end

**I test funzionali H2 colmano questo gap.**

### 1.2 Cosa coprono i test funzionali

| Aspetto | Esempio |
|---|---|
| **Query SQL reali** | `SELECT`, `INSERT`, `UPDATE`, `DELETE` eseguiti su H2 |
| **Comportamento runtime** | Verifica che il risultato di una query sia quello atteso |
| **SQL Injection neutralizzata** | Payload `' OR '1'='1` trattato come stringa letterale |
| **Interazione Servlet-DAO** | La Servlet passa i parametri giusti al DAO |
| **Gestione eccezioni DB** | NumberFormatException, NPE, DataAccessException |
| **Violazioni vincoli** | Foreign key, PRIMARY KEY, NOT NULL |
| **Hashing SHA-1** | Verifica del doppio hash nel bean `Utente` |

### 1.3 Cosa NON coprono i test funzionali

| Componente | Perché non testato funzionalmente |
|---|---|
| **Bean / POJO** | Non interagiscono col DB (Prodotto, Carta, AcquistoProdotti) |
| **Helper** | Non interagiscono col DB (JspHelper, ServletErrorHelper) |
| **Servlet di sola presentazione** | Solo forward a JSP (Registrazione, CarrelloServlet, InizioServlet per alcuni rami) |
| **ConPool** | Infrastruttura — verificata con analisi statica |
| **DataAccessException** | Eccezione custom — testata separatamente (2 test) |

---

## 2. Infrastruttura H2

### 2.1 Architettura

L'infrastruttura H2 è composta da:

```
src/test/
├── java/security/functional/
│   ├── BaseH2Test.java              ← infrastruttura DAO
│   ├── BaseServletH2Test.java       ← infrastruttura Servlet (estende BaseH2Test)
│   ├── TestFunctions.java           ← SHA1 in Java per alias H2
│   ├── dao/                         ← test funzionali DAO
│   ├── exceptions/                  ← test DataAccessException
│   ├── authorization/               ← test Servlet A01
│   ├── businesslogic/               ← test Servlet A04
│   └── inputvalidation/             ← test Servlet A03
└── resources/
    └── schema-h2.sql                ← schema DB H2 (adattato da Pharmatex.sql)
```

### 2.2 File di supporto

| File | Ruolo |
|---|---|
| `schema-h2.sql` | Schema DB (adattato da `Pharmatex.sql`) compatibile con H2 `MODE=MySQL` |
| `TestFunctions.java` | Implementazione Java di `SHA1()`, registrata come alias di funzione in H2 |
| `BaseH2Test.java` | Classe base astratta: attiva il test mode, esegue lo schema, pulisce il DB prima di ogni test |
| `BaseServletH2Test.java` | Estende `BaseH2Test` e aggiunge i mock di `HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `RequestDispatcher` |

### 2.3 Modifiche al codice di produzione

`ConPool.java` è stato esteso con 3 elementi **additivi** (nessuna modifica alla logica esistente):

```java
private static boolean testMode = false;
private static String testUrl = null;

public static void enableTestMode(String url) { /* attiva H2 */ }
public static void disableTestMode() { /* ripristina MySQL */ }
```

Il branch `if (testMode)` in `getConnection()` è **irraggiungibile in produzione** (nessuno chiama `enableTestMode()` fuori dai test).

### 2.4 Registrazione della funzione SHA1

Poiché `UtenteDAO.doLogin` usa `SHA1(?)` (funzione MySQL-specifica), in H2 è stato registrato un alias:

```sql
CREATE ALIAS IF NOT EXISTS SHA1 FOR "security.functional.TestFunctions.sha1"
```

Da quel momento, ogni chiamata a `SHA1(x)` in H2 viene tradotta nella chiamata al metodo Java `TestFunctions.sha1(x)`, che usa `MessageDigest.getInstance("SHA-1")` — la stessa implementazione del bean `Utente`.

---

## 3. Test funzionali DAO — 38 test

### 3.1 ProdottoDAOH2Test (14 test)

**File:** `src/test/java/security/functional/dao/ProdottoDAOH2Test.java`

| # | Test | Cosa verifica |
|:---:|---|---|
| 1 | `testDoRetriveAll` | Restituisce tutti i prodotti |
| 2 | `testDoRetriveAllEmpty` | DB vuoto → lista vuota |
| 3 | `testDoRetriveByCategoria` | Filtro per categoria |
| 4 | `testDoRetriveBySearch_CaseInsensitive` | **FIX SEC-DAO-01** — case-insensitive |
| 5 | `testDoRetriveByFilter` | Filtro prezzo + categoria |
| 6 | `testDoRetriveQuantitaEsaurita` | Prodotti con quantità = 0 |
| 7 | `testAggiuntaProdotto` | INSERT prodotto |
| 8 | `testCancellaProdotto` | DELETE prodotto |
| 9 | `testDoUpdateQuantita` | UPDATE quantità |
| 10 | `testDoSetNewPrezzo` | UPDATE prezzo |
| 11 | `testDoRetriveMaterialeMaterasso` | Filtro per tipo materiale |
| 12 | `testDoRetriveMaterialeRete` | Filtro per materiale rete |
| 13 | `testDoRetriveMaterialeCuscino` | Filtro per materiale cuscino |
| 14 | `testSqlInjectionNeutralizzata` | SQL Injection neutralizzata dal PreparedStatement |

### 3.2 UtenteDAOH2Test (13 test)

**File:** `src/test/java/security/functional/dao/UtenteDAOH2Test.java`

| # | Test | Cosa verifica |
|:---:|---|---|
| 1 | `testDoLoginCorretto` | Login con SHA1 |
| 2 | `testDoLoginPasswordErrata` | Password errata → null |
| 3 | `testDoLoginEmailInesistente` | Email inesistente → null |
| 4 | `testDoLoginAdmin` | Login admin |
| 5 | `testControlloEmailEsistente` | Email esistente |
| 6 | `testControlloEmailInesistente` | Email inesistente |
| 7 | `testDoRetriveUtente` | Tutti gli utenti |
| 8 | `testDoRetriveUtenteVuoto` | DB vuoto |
| 9 | `testRendiAmministratore` | Promozione admin |
| 10 | `testRimuoviAmministratore` | Declassamento |
| 11 | `testDoRegistrazione` | INSERT utente |
| 12 | `testRegistrazionePoiLogin_EndToEnd` | Registrazione + Login E2E |
| 13 | `testSqlInjectionLogin` | SQL Injection neutralizzata |

### 3.3 CartaDAOH2Test (4 test)

**File:** `src/test/java/security/functional/dao/CartaDAOH2Test.java`

| # | Test | Cosa verifica |
|:---:|---|---|
| 1 | `testAggiuntaNuovaCarta` | INSERT carta |
| 2 | `testAggiuntaCartaDuplicata` | Anti-duplicati |
| 3 | `testAggiuntaDueCarteDiverse` | Due carte diverse |
| 4 | `testNumeroCartaInChiaro` | **FINDING PCI-DSS**: numero/CVV in chiaro |

### 3.4 AcquistoProdottiDAOH2Test (7 test)

**File:** `src/test/java/security/functional/dao/AcquistoProdottiDAOH2Test.java`

| # | Test | Cosa verifica |
|:---:|---|---|
| 1 | `testAcquistaProdotto` | INSERT acquisto |
| 2 | `testAcquistaProdottoEmailInesistente` | FK violata → RuntimeException |
| 3 | `testDoRetriveAcquistoUtente` | Acquisti di un utente |
| 4 | `testDoRetriveAcquistoUtenteVuoto` | Utente senza acquisti |
| 5 | `testDoRetriveAcquisto` | Tutti gli acquisti |
| 6 | `testDoRetriveAcquistoVuoto` | DB vuoto |
| 7 | `testJoinDatiCliente` | JOIN con cliente |

### 3.5 DataAccessExceptionTest (2 test)

**File:** `src/test/java/security/functional/exceptions/DataAccessExceptionTest.java`

| # | Test | Cosa verifica |
|:---:|---|---|
| 1 | `testCostruttoreConMessage` | Costruttore `DataAccessException(String)` imposta il messaggio, `cause` = null |
| 2 | `testCostruttoreConMessageECause` | Costruttore `DataAccessException(String, Throwable)` imposta messaggio + causa |

**Nota:** l'eccezione `DataAccessException` è stata introdotta come fix di SonarQube (regola S112) per sostituire `RuntimeException` generica nei DAO. È testata separatamente perché non dipende dal DB.

---

## 4. Test funzionali Servlet — 136 test

### 4.1 Authorization — 54 test

**Cartella:** `src/test/java/security/functional/authorization/`

| Servlet | Test | File |
|---|:---:|---|
| `LoginServlet` | 7 | `LoginServletH2Test.java` |
| `RegistrazioneServlet` | 6 | `RegistrazioneServletH2Test.java` |
| `CercaProdottoPerModificaServlet` | 8 | `CercaProdottoPerModificaServletH2Test.java` |
| `HomeServletAmministratore` | 9 | `HomeServletAmministratoreH2Test.java` |
| `PagamentoServlet` | 5 | `PagamentoServletH2Test.java` |
| `RendiAmministratoreServlet` | 10 | `RendiAmministratoreServletH2Test.java` |
| `RimuoviProdottoServlet` | 9 | `RimuoviProdottoServletH2Test.java` |

**Finding documentati:**
- `AggiuntaProdottoServlet` e `ModificaProdottiServletAmministratore`: **CWE-862 Missing Authorization**
- `RimuoviProdottoServlet`: prodotto in sessione ma non nel DB → RuntimeException (500)

### 4.2 Business Logic — 19 test

**Cartella:** `src/test/java/security/functional/businesslogic/`

| Servlet | Test | File |
|---|:---:|---|
| `HomeServlet` | 9 | `HomeServletH2Test.java` |
| `DatiPagamentoServlet` | 10 | `DatiPagamentoServletH2Test.java` |

**Finding documentati:**
- `DatiPagamentoServlet`: `qList.size() < cart_list.size()` → IndexOutOfBoundsException
- `DatiPagamentoServlet`: mancanza di transazione atomica (carrello svuotato anche se INSERT fallisce)

### 4.3 Input Validation — 63 test

**Cartella:** `src/test/java/security/functional/inputvalidation/`

| Servlet | Test | File |
|---|:---:|---|
| `AggiuntaProdottoServlet` | 8 | `AggiuntaProdottoServletH2Test.java` |
| `RicercaServlet` | 7 | `RicercaServletH2Test.java` |
| `MaterialeServlet` | 9 | `MaterialeServletH2Test.java` |
| `FiltraggioServletMateriale` | 10 | `FiltraggioServletMaterialeH2Test.java` |
| `FiltraggioServletPrezzo` | 10 | `FiltraggioServletPrezzoH2Test.java` |
| `ModificaProdottiServletAmministratore` | 10 | `ModificaProdottiServletAmministratoreH2Test.java` |
| `InizioServlet` | 9 | `InizioServletH2Test.java` |

**Finding documentati:**
- `RicercaServlet`: NPE su input null, doppio upper ridondante
- `FiltraggioServletPrezzo`: **SEC-IV-03** doppio forward, NPE su categoria null, NumberFormatException
- `ModificaProdottiServletAmministratore`: CWE-862, NPE su nuovoPrezzo/idModificaPrezzo null, NumberFormatException, silent drop
- `AggiuntaProdottoServlet`: CWE-862, NumberFormatException, idProdotto null

---

## 5. Finding scoperti

I test funzionali H2 hanno scoperto **bug reali** che i test unitari e l'analisi statica non potevano rilevare.

### 5.1 SEC-DAO-01 — doRetriveBySearch non case-insensitive

| ID | Vulnerabilità | CWE | Classe | Test | Stato |
|----|---------------|:---:|--------|------|:-----:|
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive: la query usava `upper(nomeProd) LIKE ?` senza uppercasare il pattern | CWE-178 | `ProdottoDAO` | `testDoRetriveBySearch_CaseInsensitive` | Risolto |

**Patch applicata:** modificata la query in `upper(nomeProd) LIKE upper(?)` per rendere la ricerca case-insensitive anche sui pattern in input.

**Nota metodologica:** il bug è stato scoperto **solo** grazie al test funzionale su H2. Né SonarQube né l'analisi statica potevano rilevarlo, poiché si tratta di un bug di comportamento a runtime e non di un pattern statico rilevabile. Questo conferma il valore aggiunto dell'approccio funzionale nel colmare il gap tra analisi statica e verifica comportamentale.

### 5.2 SEC-IV-03 — Doppio forward in FiltraggioServletPrezzo

| ID | Finding | CWE | Classe | Test | Stato |
|----|---------|:---:|--------|------|:-----:|
| SEC-IV-03 | Doppio forward quando entrambi i prezzi sono vuoti | CWE-754 | `FiltraggioServletPrezzo` | `testDoppioForward_BugDocumentato` | Documentato |

**Descrizione:** nel ramo "prezzomin == '' AND prezzomax == ''", la Servlet esegue il forward a `RicercaErrata.jsp` ma **non ha un `return`**, quindi prosegue e chiama un secondo forward a `Prodotti.jsp` con `filterList = null`.

**Nota importante:** questo è **l'ennesimo bug trovato grazie ai test funzionali** (come SEC-DAO-01). In un container reale (Tomcat) causerebbe `IllegalStateException: Cannot forward after response has been committed`.

**Patch suggerita (non applicata):** aggiungere `return;` dopo il primo forward, come già fatto in `MaterialeServlet` (fix SEC-IV-02).

### 5.3 Altri finding documentati

| ID | Finding | CWE | Classe | Stato |
|----|---------|:---:|--------|:-----:|
| SEC-DAO-02 | Numero carta e CVV salvati in chiaro (PCI DSS 3.2) | CWE-312 | `CartaDAO` | Documentato |
| SEC-DAO-03 | Doppio hashing nel setter `Utente.setPassword()` | CWE-1064 | `Utente.java` | Documentato |
| FUNC-AUTH-01 | Nessun controllo autorizzazione in `AggiuntaProdottoServlet` e `ModificaProdottiServletAmministratore` | CWE-862 | 2 Servlet | Documentato |
| FUNC-IV-01 | NPE su parametri null in `RicercaServlet`, `FiltraggioServletPrezzo`, `ModificaProdottiServletAmministratore` | CWE-476 | 3 Servlet | Documentato |
| FUNC-IV-02 | NumberFormatException su input non numerici | CWE-20 | 3 Servlet | Documentato |
| FUNC-IV-03 | Silent drop quando nessun ramo viene eseguito | — | `ModificaProdottiServletAmministratore` | Documentato |
| FUNC-BL-01 | IndexOutOfBoundsException se `qList.size() < cart_list.size()` | — | `DatiPagamentoServlet` | Documentato |
| FUNC-BL-02 | Doppio forward in `RimuoviProdottoServlet` | CWE-754 | `RimuoviProdottoServlet` | Documentato |

---

## 6. Metriche finali

### 6.1 Test funzionali per area

| Area | DAO | Servlet | Eccezioni | Totale |
|---|:---:|:---:|:---:|:---:|
| Authorization | — | 54 | — | **54** |
| Business Logic | — | 19 | — | **19** |
| Input Validation | — | 63 | — | **63** |
| DAO (tutte le operazioni) | 38 | — | — | **38** |
| Eccezioni custom | — | — | 2 | **2** |
| **TOTALE** | **38** | **136** | **2** | **176** |

### 6.2 Metriche di questa categoria

| Metrica | Valore |
|:---|:---:|
| **Test funzionali totali** | **176** |
| **Classi di test** | **21** |
| **Contributo coverage** | **~85%** |
| **Success rate** | **100%** |
| **Durata media esecuzione** | ~3-5 secondi |

### 6.3 Finding scoperti da questa categoria

| Categoria | Finding |
|---|:---:|
| Bug risolti | 1 (SEC-DAO-01) |
| Bug documentati | 9 |
| **TOTALE** | **10** |

---

## 7. Dettaglio vulnerabilità critiche

Questa sezione documenta in dettaglio le vulnerabilità critiche emerse **solo grazie ai test funzionali H2**. Per ogni vulnerabilità sono descritti: contesto, scenario osservato e patch applicata o raccomandata.

### 7.1 SEC-DAO-01 — doRetriveBySearch non case-insensitive

- **File:** `src/main/java/Model/ProdottoDAO.java`
- **Test:** `testDoRetriveBySearch_CaseInsensitive` (`ProdottoDAOH2Test`)
- **CWE:** [CWE-178](https://cwe.mitre.org/data/definitions/178.html) (Improper Handling of Case Sensitivity)
- **Severità:** Critica
- **Stato:** Risolto

**Vulnerabilità**

Il metodo `doRetriveBySearch` eseguiva la query `SELECT ... WHERE upper(nomeProd) LIKE ?`, uppercasando solo la colonna `nomeProd` ma non il pattern di ricerca passato come parametro. Di conseguenza, la ricerca era case-insensitive solo per il campo del database, mentre il pattern inviato dall'utente veniva confrontato letteralmente.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Ricerca "Materasso" | OK |
| Ricerca "MATERASSO" | Lista vuota (bug) |
| Ricerca "materasso" | Lista vuota (bug) |

**Patch applicata**

Modificata la query in `upper(nomeProd) LIKE upper(?)` per uppercasare anche il pattern di ricerca. Il test `testDoRetriveBySearch_CaseInsensitive` verifica il fix con diverse combinazioni di maiuscole/minuscole.

**Nota metodologica:** questo bug era **invisibile** a SonarQube e all'analisi statica, perché richiedeva l'esecuzione reale della query contro un database. È stato scoperto **solo** grazie ai test funzionali H2.

---

### 7.2 SEC-IV-03 — Doppio forward in FiltraggioServletPrezzo

- **File:** `src/main/java/Controller/FiltraggioServletPrezzo.java`
- **Test:** `testDoppioForward_BugDocumentato` (`FiltraggioServletPrezzoH2Test`)
- **CWE:** [CWE-754](https://cwe.mitre.org/data/definitions/754.html) (Improper Check for Unusual or Exceptional Conditions)
- **Severità:** Critica
- **Stato:** Documentato

**Vulnerabilità**

Nel ramo "prezzomin == '' AND prezzomax == ''", la Servlet esegue il forward a `RicercaErrata.jsp` ma **non ha un `return`**, quindi prosegue e chiama un secondo forward a `Prodotti.jsp` con `filterList = null`. In un container reale (Tomcat) questo comportamento genera `IllegalStateException: Cannot forward after response has been committed`.

**Scenario**

| Scenario | Comportamento |
|---|---|
| `prezzomin` e `prezzomax` valorizzati | Singolo forward → OK |
| Entrambi i prezzi vuoti | Doppio forward → **IllegalStateException** |
| Con patch applicata | `return` dopo il primo forward → OK |

**Patch suggerita (non applicata)**

Aggiungere `return;` dopo il primo `forward()` nel ramo "entrambi vuoti", replicando il fix già applicato in `MaterialeServlet` (SEC-IV-02). Il bug è **documentato** dal test `testDoppioForward_BugDocumentato` che funge da regression protection.

---

### 7.3 FUNC-AUTH-01 — Missing Authorization in 2 Servlet

- **File:**
    - `src/main/java/Controller/AggiuntaProdottoServlet.java`
    - `src/main/java/Controller/ModificaProdottiServletAmministratore.java`
- **Test:** vari test nelle classi `AggiuntaProdottoServletH2Test`, `ModificaProdottiServletAmministratoreH2Test`
- **CWE:** [CWE-862](https://cwe.mitre.org/data/definitions/862.html) (Missing Authorization)
- **Severità:** Critica
- **Stato:** Documentato

**Vulnerabilità**

Due Servlet amministrative non applicavano alcun controllo di autorizzazione. Chiunque, anche un utente anonimo, poteva invocare direttamente l'URL per aggiungere o modificare prodotti, alterando il catalogo del sito.

**Scenario**

| Scenario | Comportamento prima | Comportamento atteso |
|---|---|---|
| Admin accede a `/AggiuntaProdottoServlet` | OK | OK |
| Anonimo accede a `/AggiuntaProdottoServlet` | **Prodotto aggiunto** | HTTP 403 |
| Anonimo accede a `/ModificaProdottiServletAmministratore` | **Prodotto modificato** | HTTP 403 |

**Patch raccomandata**

Aggiungere in testa al metodo `doPost` un controllo di autorizzazione:

```java
Utente admin = (Utente) session.getAttribute("Amministratore");
if (admin == null || !admin.isAmministratore()) {
    ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato");
    return;
}
```

Il controllo va replicato in entrambe le Servlet. I test funzionali H2 fungono da regression protection per verificare che l'autorizzazione venga effettivamente applicata.

---

### 7.4 FUNC-IV-01 — NPE su parametri null in 3 Servlet

- **File:**
    - `src/main/java/Controller/RicercaServlet.java`
    - `src/main/java/Controller/FiltraggioServletPrezzo.java`
    - `src/main/java/Controller/ModificaProdottiServletAmministratore.java`
- **Test:** vari test nelle classi `RicercaServletH2Test`, `FiltraggioServletPrezzoH2Test`, `ModificaProdottiServletAmministratoreH2Test`
- **CWE:** [CWE-476](https://cwe.mitre.org/data/definitions/476.html) (NULL Pointer Dereference)
- **Severità:** Alta
- **Stato:** Documentato

**Vulnerabilità**

Tre Servlet accedono a parametri della request chiamando metodi come `equals("")`, `equalsIgnoreCase()`, `parseDouble()` **senza verificare che il parametro non sia `null`**. Un attaccante che invia la richiesta omettendo il parametro causa un `NullPointerException` → HTTP 500, con esposizione dello stack trace nei log applicativi.

**Scenario**

| Servlet | Scenario | Comportamento |
|---|---|---|
| `RicercaServlet` | Richiesta senza `search` | NPE → 500 |
| `FiltraggioServletPrezzo` | Richiesta senza `prezzomin` | NPE → 500 |
| `ModificaProdottiServletAmministratore` | Richiesta senza `nuovoPrezzo` | NPE → 500 |

**Patch raccomandata**

Aggiungere un null check per ogni parametro subito dopo `request.getParameter()`:

```java
String search = request.getParameter("search");
if (search == null) {
    ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro mancante");
    return;
}
```

Il pattern va replicato per ogni parametro critico. I test funzionali H2 documentano il comportamento attuale e fungono da regression protection.

---

### 7.5 FUNC-IV-02 — NumberFormatException su input non numerici

- **File:**
    - `src/main/java/Controller/FiltraggioServletPrezzo.java`
    - `src/main/java/Controller/ModificaProdottiServletAmministratore.java`
    - `src/main/java/Controller/AggiuntaProdottoServlet.java`
- **Test:** vari test nelle classi H2Test corrispondenti
- **CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html) (Improper Input Validation)
- **Severità:** Alta
- **Stato:** Documentato

**Vulnerabilità**

Le Servlet chiamano `Double.parseDouble()` e `Integer.parseInt()` sui parametri della request senza verificarne il formato. Un input non numerico causa `NumberFormatException` → HTTP 500.

**Scenario**

| Input | Comportamento |
|---|---|
| `prezzomin=100` | OK |
| `prezzomin=abc` | NumberFormatException → 500 |
| `quantitaTotale=1; DROP TABLE` | NumberFormatException → 500 |

**Patch raccomandata**

Avvolgere il parsing in un `try/catch` esplicito oppure pre-validare con espressione regolare prima del parsing:

```java
try {
    double valore = Double.parseDouble(prezzo);
    // ... logica
} catch (NumberFormatException e) {
    ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Formato non valido");
    return;
}
```

---

### 7.6 FUNC-IV-03 — Silent drop in ModificaProdottiServletAmministratore

- **File:** `src/main/java/Controller/ModificaProdottiServletAmministratore.java`
- **Test:** `testNessunRamoEseguitoConParametriVuoti` (`ModificaProdottiServletAmministratoreH2Test`)
- **CWE:** — (design smell)
- **Severità:** Media
- **Stato:** Documentato

**Vulnerabilità**

Quando `nuovoPrezzo=""` e `quantitaTotale=null`, nessuno dei tre rami della Servlet viene eseguito e la Servlet **non fa nulla silenziosamente**. L'utente che invia una form malformata non riceve alcun feedback né un redirect, causando una pagina vuota o un errore opaco.

**Scenario**

| Scenario | Comportamento |
|---|---|
| `nuovoPrezzo=99&quantitaTotale=5` | Aggiorna prezzo e quantità |
| `nuovoPrezzo=""` e `quantitaTotale=null` | **Nessun effetto, nessun feedback** |
| Con patch | Redirect a HomeServletAmministratore |

**Patch raccomandata**

Aggiungere un `else` finale che redirige esplicitamente o mostra un errore:

```java
} else {
    ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Nessun parametro fornito");
}
```

---

### 7.7 FUNC-BL-01 — IndexOutOfBoundsException in DatiPagamentoServlet

- **File:** `src/main/java/Controller/DatiPagamentoServlet.java`
- **Test:** vari test in `DatiPagamentoServletH2Test`
- **CWE:** — (bug logico)
- **Severità:** Alta
- **Stato:** Documentato

**Vulnerabilità**

La Servlet itera su `cart_list` accedendo a `qList` per indice, senza verificare che `qList.size() >= cart_list.size()`. Se la sessione contiene uno stato inconsistente (es. dopo un'aggiunta parziale al carrello), la Servlet lancia `IndexOutOfBoundsException` → HTTP 500.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Carrello e quantità coerenti | OK |
| `qList.size() < cart_list.size()` | IndexOutOfBoundsException → 500 |

**Patch raccomandata**

Verificare la coerenza delle due liste prima dell'iterazione:

```java
if (qList.size() != cart_list.size()) {
    ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Stato carrello inconsistente");
    return;
}
```

---

### 7.8 FUNC-BL-02 — Doppio forward in RimuoviProdottoServlet

- **File:** `src/main/java/Controller/RimuoviProdottoServlet.java`
- **Test:** vari test in `RimuoviProdottoServletH2Test`
- **CWE:** [CWE-754](https://cwe.mitre.org/data/definitions/754.html) (Improper Check for Unusual or Exceptional Conditions)
- **Severità:** Media
- **Stato:** Documentato

**Vulnerabilità**

In alcune condizioni (es. prodotto non trovato nel carrello), la Servlet esegue un primo `forward()` e prosegue senza `return`, causando un secondo `forward()` → `IllegalStateException` in container reale.

**Scenario**

| Scenario | Comportamento |
|---|---|
| Prodotto rimosso con successo | Singolo forward → OK |
| Prodotto non trovato | Doppio forward → IllegalStateException |

**Patch raccomandata**

Aggiungere `return;` dopo ogni `forward()`, come già fatto in `MaterialeServlet` (SEC-IV-02).

---

### 7.9 Riepilogo vulnerabilità critiche

| ID | Vulnerabilità | CWE | Severità | Stato |
|----|---------------|:---:|:--------:|:-----:|
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive | CWE-178 | Critica | Risolto |
| SEC-IV-03 | Doppio forward in `FiltraggioServletPrezzo` | CWE-754 | Critica | Documentato |
| FUNC-AUTH-01 | Missing Authorization in 2 Servlet | CWE-862 | Critica | Documentato |
| FUNC-IV-01 | NPE su parametri null in 3 Servlet | CWE-476 | Alta | Documentato |
| FUNC-IV-02 | NumberFormatException su input non numerici | CWE-20 | Alta | Documentato |
| FUNC-IV-03 | Silent drop | — | Media | Documentato |
| FUNC-BL-01 | IndexOutOfBoundsException in `DatiPagamentoServlet` | — | Alta | Documentato |
| FUNC-BL-02 | Doppio forward in `RimuoviProdottoServlet` | CWE-754 | Media | Documentato |

**Nota:** per i finding **SEC-DAO-02** (CVV in chiaro) e **SEC-DAO-03** (doppio hashing), già documentati nel report complementare [`unit.md`](unit.md), si rimanda alle sezioni 3.3 e 7.3 di quel documento.

---

## 8. Nota metodologica

### 8.1 Perché H2 in-memory

La scelta di usare **H2 in-memory** invece di un DB reale (MySQL in Docker, Testcontainers) è motivata da:

| Criterio | H2 in-memory | Testcontainers |
|---|:---:|:---:|
| Velocità di esecuzione | ~10-100 ms per test | ~500 ms (startup container) |
| Richiede Docker | No | Sì |
| Eseguibile in CI/CD | Ovunque | Solo con Docker |
| Compatibile con Java 8 | H2 2.2.224 | Richiede Java 11+ |
| Fedeltà al DB di produzione | ~95% (MODE=MySQL) | 100% |

### 8.2 Limiti dell'approccio

**H2 non è MySQL al 100%.** Alcune differenze:

- Funzioni specifiche (`SHA1()`) → risolte con `CREATE ALIAS`
- Sintassi `RESTART IDENTITY` → supportata in `MODE=MySQL`
- Alcune ottimizzazioni del query planner → diverse

Per il progetto attuale, queste differenze **non impattano** i test funzionali.