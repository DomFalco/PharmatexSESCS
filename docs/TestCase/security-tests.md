# Security Test Suite — PharmatexSESCS

**Progetto:** PharmatexSESCS — E-commerce di prodotti per il riposo  
**Corso:** Software Engineering for Secure Cloud Systems  
**Data:** 07/10/2026

---

## Indice

- [1. Panoramica](#1-panoramica)
- [2. Perché due categorie di test?](#2-perché-due-categorie-di-test)
    - [2.1 Limiti dell'analisi statica](#21-limiti-dellanalisi-statica)
    - [2.2 Limiti dei test funzionali](#22-limiti-dei-test-funzionali)
    - [2.3 La strategia ibrida](#23-la-strategia-ibrida)
- [3. Organizzazione della documentazione](#3-organizzazione-della-documentazione)
    - [3.1 Struttura dei package](#31-struttura-dei-package)
- [4. Metriche complessive](#4-metriche-complessive)
    - [4.1 Contributo alla coverage](#41-contributo-alla-coverage)
- [5. Copertura per area OWASP](#5-copertura-per-area-owasp)
    - [5.1 Dettaglio per categoria](#51-dettaglio-per-categoria)
- [6. Finding documentati](#6-finding-documentati)
    - [6.1 Vulnerabilità risolte](#61-vulnerabilità-risolte)
    - [6.2 Finding documentati (rischio accettato)](#62-finding-documentati-rischio-accettato)
- [7. Link ai documenti di dettaglio](#7-link-ai-documenti-di-dettaglio)
    - [7.1 Test Unitari e Analisi Statica — unit.md](#71-test-unitari-e-analisi-statica--unitmd)
    - [7.2 Test Funzionali H2 — functional.md](#72-test-funzionali-h2--functionalmd)
- [8. Note metodologiche finali](#8-note-metodologiche-finali)
    - [8.1 Perché i test funzionali sono separati dai test unitari](#81-perché-i-test-funzionali-sono-separati-dai-test-unitari)
    - [8.2 Il valore aggiunto dei test funzionali H2](#82-il-valore-aggiunto-dei-test-funzionali-h2)

---

## 1. Panoramica

La suite di test di sicurezza di **PharmatexSESCS** è composta da **481 test** distribuiti su **2 categorie complementari** e **5 aree OWASP** (come da OWASP Testing Guide).

| Categoria | Approccio | Contributo Coverage |
|-----------|-----------|:-------------------:|
| **Test Unitari + Analisi Statica** | Analizza il codice sorgente come testo (DAO) e usa Mockito per le Servlet | Non contribuisce direttamente |
| **Test Funzionali H2** | Esegue il codice di produzione con Mockito + database H2 in-memory | **Contribuisce** (JaCoCo) |

Le due categorie sono **complementari e non ridondanti**:

- I **test unitari** verificano la logica di controllo (autorizzazione, redirect, forward) e documentano pattern di sicurezza nei DAO tramite analisi statica del sorgente.
- I **test funzionali H2** verificano il comportamento a runtime del codice contro un database reale, scoprendo bug che né SonarQube né l'analisi statica possono rilevare.

**Stato complessivo:**

- **Quality Gate SonarCloud:** PASSED
- **Coverage sul New Code:** 84.76% (soglia ≥ 80%)
- **Success rate:** 100%
- **Durata media esecuzione:** ~3-5 secondi

---

## 2. Perché due categorie di test?

La scelta di utilizzare **due approcci paralleli** non è casuale, ma risponde a una precisa strategia di sicurezza.

### 2.1 Limiti dell'analisi statica

L'analisi statica del sorgente è potente per trovare pattern noti ma ha limiti:

- **Non esegue il codice** → non rileva bug runtime
- **Può produrre falsi positivi** (analisi testuale, non AST)
- **Non contribuisce alla coverage SonarCloud**
- **Non verifica il comportamento effettivo** del sistema

### 2.2 Limiti dei test funzionali

I test funzionali verificano il comportamento ma non coprono tutto:

- **Non rilevano finding strutturali** (es. pattern di sicurezza nel sorgente)
- **Richiedono setup** (Mockito + H2) più complesso
- **Non documentano il "perché"** di un finding, solo il comportamento osservato

### 2.3 La strategia ibrida

Combinando i due approcci:

| Obiettivo | Strumento | Vantaggio |
|-----------|-----------|-----------|
| Trovare SQL Injection (pattern) | Analisi statica | Analisi di ogni query nel sorgente |
| Verificare SQL Injection a runtime | Test funzionale H2 | Esegue query con payload reale |
| Documentare NPE su input null | Test unitario Mockito | Simula il web container |
| Alimentare coverage | Test funzionale H2 | JaCoCo conta le righe eseguite |
| Prevenire regressioni | Entrambi | Se il codice cambia, i test falliscono |
| Scoprire bug di comportamento | Test funzionale H2 | **SEC-DAO-01 e SEC-IV-03** |

Il valore aggiunto è dimostrato da **2 bug reali** scoperti solo grazie ai test funzionali H2, invisibili all'analisi statica e a SonarQube.

---

## 3. Organizzazione della documentazione

Per mantenere la documentazione leggibile e navigabile, i dettagli sono separati in **2 file di dettaglio**, ciascuno dedicato a una categoria:

```
docs/
└── TestCase/
    ├── security-test.md    ← QUESTO FILE (panoramica + hub)
    ├── unit.md             ← Dettaglio dei 305 test unitari e analisi statica
    └── functional.md       ← Dettaglio dei 176 test funzionali H2
```

### 3.1 Struttura dei package

La separazione si riflette anche nel codice sorgente:

```
src/test/java/
└── security/
    ├── unit/                           ← 305 test unitari (29 file)
    │   ├── authorization/              ← A01 — 75 test
    │   ├── businesslogic/              ← A04 — 78 test
    │   ├── daointegration/             ← A03 — 49 test (analisi statica)
    │   ├── dataprotection/             ← A02 — 20 test
    │   └── inputvalidation/            ← A03 — 83 test
    │
    └── functional/                     ← 176 test funzionali H2 (21 file)
        ├── BaseH2Test.java
        ├── BaseServletH2Test.java
        ├── TestFunctions.java
        ├── authorization/              ← A01/A07 — 54 test
        ├── businesslogic/              ← A04 — 19 test
        ├── daointegration/             ← A03 — 38 test
        ├── exceptions/                 ← 2 test
        └── inputvalidation/            ← A03 — 63 test
```

**Convenzione di naming:**

| Categoria | Convenzione | Esempio |
|-----------|-------------|---------|
| Test unitari | `*Test.java` | `UtenteDAOTest.java` |
| Test funzionali | `*H2Test.java` | `UtenteDAOH2Test.java` |

Questa convenzione permette di distinguere i due approcci già dal nome del file.

---

## 4. Metriche complessive

| Metrica | Valore |
|---------|:------:|
| **Test totali** | **481** |
| Test unitari e analisi statica | **305** (29 classi) |
| Test funzionali H2 | **176** (21 classi) |
| Aree OWASP coperte | **5** |
| Vulnerabilità risolte | **14** |
| Finding documentati (rischio accettato) | **~15** |
| Coverage SonarCloud (New Code) | **84.76%** |
| Success rate | **100%** |
| Durata media esecuzione | ~3-5 secondi |
| Quality Gate SonarCloud | **Passed** |

### 4.1 Contributo alla coverage

I **test funzionali H2** sono quelli che contribuiscono direttamente alla coverage SonarCloud, perché **eseguono il codice di produzione** contro un database in-memory.

I **test unitari** non contribuiscono direttamente alla coverage (usano Mockito e analisi statica), ma sono essenziali per:

- Documentare pattern di sicurezza nei DAO (analisi statica)
- Verificare la logica di controllo delle Servlet (autorizzazione, redirect, forward)
- Documentare vulnerabilità note (NPE, NumberFormatException, doppio forward)

---

## 5. Copertura per area OWASP

| Area OWASP | Test Unitari | Test Funzionali | Totale |
|-----------|:------------:|:---------------:|:------:|
| **A01 — Broken Access Control** | 75 | 54 | **129** |
| **A02 — Cryptographic Failures** | 20 | — | **20** |
| **A03 — Injection** | 132 | 101 | **233** |
| **A04 — Insecure Design** | 78 | 19 | **97** |
| **Exceptions** | — | 2 | **2** |
| **TOTALE** | **305** | **176** | **481** |

### 5.1 Dettaglio per categoria

| Categoria | Focus | Test |
|-----------|-------|:----:|
| **A01 — Broken Access Control** | Autorizzazione, sessioni, privilegi | 129 |
| **A02 — Cryptographic Failures** | Hashing, credenziali, dati sensibili | 20 |
| **A03 — Injection** | Sanitizzazione, XSS, SQL Injection, pattern statici | 233 |
| **A04 — Insecure Design** | Logica applicativa, flussi, calcoli | 97 |
| **Exceptions** | DataAccessException custom | 2 |

---

## 6. Finding documentati

La suite ha identificato **14 vulnerabilità risolte** e **~15 finding documentati come rischio accettato**. La seguente tabella riassume i finding di sicurezza più rilevanti.

Per il dettaglio completo di ogni vulnerabilità critica (contesto, scenario osservato, patch applicata o raccomandata), consulta:

- **Vulnerabilità su test unitari** → [`unit.md` § 8](unit.md#8-dettaglio-vulnerabilità-critiche)
- **Vulnerabilità su test funzionali H2** → [`functional.md` § 7](functional.md#7-dettaglio-vulnerabilità-critiche)

### 6.1 Vulnerabilità risolte

| ID | Vulnerabilità | CWE | Endpoint / File | Tipo test | Stato | Dettaglio |
|----|---------------|:---:|-----------------|:---------:|:-----:|:---------:|
| SEC-IV-01 | NPE su `mat.equalsIgnoreCase()` | CWE-476 | `/MaterialeServlet` | Unit | Risolto | [unit.md § 8.1](unit.md#81-npe-su-matequalsignorecase-in-materialeservlet-sec-iv-01) |
| SEC-IV-02 | Doppio forward | CWE-754 | `/MaterialeServlet` | Unit | Risolto | [unit.md § 8.2](unit.md#82-doppio-forward-in-materialeservlet-sec-iv-02) |
| SEC-IV-03 | Doppio forward | CWE-754 | `/FiltraggioServletPrezzo` | Funzionale H2 | Documentato | [functional.md § 7.2](functional.md#72-sec-iv-03--doppio-forward-in-filtraggioservletprezzo) |
| IV-01 | Sanitizzazione input assente | CWE-79 | `/FiltraggioServletMateriale` | Unit | Risolto | [unit.md § 8.3](unit.md#83-sanitizzazione-input-assente-in-filtraggioservletmateriale-iv-01) |
| IV-02 | Validazione regex assente | CWE-20 | `/InizioServlet` | Unit | Risolto | [unit.md § 8.4](unit.md#84-validazione-regex-assente-in-inizioservlet-iv-02) |
| AUTH-01÷06 | Missing Authorization | CWE-862 | 6 Servlet | Unit + Funzionale H2 | Risolto | [unit.md § 8.5](unit.md#85-missing-authorization-in-6-servlet-auth-0106) |
| AUTH-07 | Matching permissivo `contains` | CWE-20 | `/RendiAmministratoreServlet` | Unit | Risolto | [unit.md § 8.6](unit.md#86-matching-permissivo-in-rendiamministratoreservlet-auth-07) |
| BL-01, BL-02 | Accesso non autorizzato | CWE-862 | 2 Servlet | Unit | Risolto | [unit.md § 8.7](unit.md#87-accesso-non-autorizzato-in-homeservlet-e-datipagamentoservlet-bl-01-bl-02) |
| SEC-DAO-01 | `doRetriveBySearch` non case-insensitive | CWE-178 | `ProdottoDAO` | Funzionale H2 | Risolto | [functional.md § 7.1](functional.md#71-sec-dao-01--doretrivebysearch-non-case-insensitive) |
| SEC-DAO-07 | Eccezioni SQL wrappate in custom | CWE-391 | Tutti i DAO | Unit | Risolto | [unit.md § 8.8](unit.md#88-riepilogo-vulnerabilità-critiche) |

### 6.2 Finding documentati (rischio accettato)

| ID | Finding | CWE | File | Tipo test | Dettaglio |
|----|---------|:---:|------|:---------:|:---------:|
| IV-03 | NPE su input null | CWE-476 | `RicercaServlet` | Unit | [unit.md § 8](unit.md#8-dettaglio-vulnerabilità-critiche) |
| IV-04 | NumberFormatException non gestita | CWE-20 | `FiltraggioServletPrezzo` | Unit | [unit.md § 8](unit.md#8-dettaglio-vulnerabilità-critiche) |
| SEC-DAO-02 | Numero carta/CVV in chiaro | CWE-312 | `CartaDAO` | Unit + H2 | [unit.md § 7.3](unit.md#73-finding-documentati) |
| SEC-DAO-03 | Doppio hashing in `setPassword()` | CWE-1064 | `Utente.java` | Unit | [unit.md § 7.3](unit.md#73-finding-documentati) |
| SEC-DAO-04 | `doLogin` usa `SHA1()` | CWE-916 | `UtenteDAO` | Unit + H2 | [unit.md § 7.3](unit.md#73-finding-documentati) |
| SEC-DAO-05 | `CartaDAO` SELECT senza WHERE | CWE-770 | `CartaDAO` | Unit | [unit.md § 7.3](unit.md#73-finding-documentati) |
| SEC-DAO-06 | DAO estendono `HttpServlet` (design smell) | — | `ProdottoDAO`, `UtenteDAO` | Unit | [unit.md § 7.3](unit.md#73-finding-documentati) |
| FUNC-AUTH-01 | Missing Authorization in 2 Servlet | CWE-862 | 2 Servlet | Funzionale H2 | [functional.md § 7.3](functional.md#73-func-auth-01--missing-authorization-in-2-servlet) |
| FUNC-IV-01 | NPE su parametri null | CWE-476 | 3 Servlet | Funzionale H2 | [functional.md § 7.4](functional.md#74-func-iv-01--npe-su-parametri-null-in-3-servlet) |
| FUNC-IV-02 | NumberFormatException su input non numerici | CWE-20 | 3 Servlet | Funzionale H2 | [functional.md § 7.5](functional.md#75-func-iv-02--numberformatexception-su-input-non-numerici) |
| FUNC-IV-03 | Silent drop quando nessun ramo eseguito | — | `ModificaProdottiServletAmministratore` | Funzionale H2 | [functional.md § 7.6](functional.md#76-func-iv-03--silent-drop-in-modificaprodottiservletamministratore) |
| FUNC-BL-01 | IndexOutOfBoundsException | — | `DatiPagamentoServlet` | Funzionale H2 | [functional.md § 7.7](functional.md#77-func-bl-01--indexoutofboundsexception-in-datipagamentoservlet) |
| FUNC-BL-02 | Doppio forward in `RimuoviProdottoServlet` | CWE-754 | `RimuoviProdottoServlet` | Funzionale H2 | [functional.md § 7.8](functional.md#78-func-bl-02--doppio-forward-in-rimuoviprodottoservlet) |
| ACC-01 | SHA-1 per password (accettato) | CWE-916 | `Utente.java` | Unit | [unit.md § 3.3](unit.md#33-vulnerabilità-documentate) |
| ACC-02 | CSRF assente (accettato) | CWE-352 | Tutte le Servlet | Unit | [unit.md § 5.4](unit.md#54-vulnerabilità-documentate-rischio-accettato) |
| ACC-03 | Brute-force protection assente (accettato) | CWE-307 | `LoginServlet` | Unit | [unit.md § 5.4](unit.md#54-vulnerabilità-documentate-rischio-accettato) |
| ACC-04 | Session fixation (accettato) | CWE-384 | `LoginServlet` | Unit | [unit.md § 5.4](unit.md#54-vulnerabilità-documentate-rischio-accettato) |

---

## 7. Link ai documenti di dettaglio

Per una trattazione completa dei test, consulta:

### 7.1 Test Unitari e Analisi Statica — [`unit.md`](unit.md)

**Contenuto:**

- **305 test unitari** distribuiti su **29 classi**
- **5 sotto-sezioni OWASP**: Data Protection, Input Validation, Authorization, Business Logic, DAO Security
- **Pattern di test utilizzati**: Mockito per Servlet, chiamate dirette per Bean, analisi statica del sorgente per DAO
- **Dettaglio delle vulnerabilità critiche**: sezione 8 con contesto, scenario e patch per ogni vulnerabilità risolta
- **Finding documentati**: NPE, NumberFormatException, SHA-1, violazioni PCI DSS

### 7.2 Test Funzionali H2 — [`functional.md`](functional.md)

**Contenuto:**

- **176 test funzionali H2 in-memory** distribuiti su **21 classi**
- **Test DAO (38)**: ProdottoDAO, UtenteDAO, CartaDAO, AcquistoProdottiDAO, DataAccessException
- **Test Servlet (136)**: Authorization (54), Business Logic (19), Input Validation (63)
- **Infrastruttura H2**: `BaseH2Test`, `BaseServletH2Test`, `TestFunctions`, `schema-h2.sql`
- **Dettaglio delle vulnerabilità critiche**: sezione 7 con contesto, scenario e patch per ogni finding scoperto dai test H2
- **Finding scoperti grazie ai test H2**: SEC-DAO-01 (case-sensitivity), SEC-IV-03 (doppio forward)

---

## 8. Note metodologiche finali

### 8.1 Perché i test funzionali sono separati dai test unitari

I test funzionali H2 sono documentati separatamente perché:

1. **Usano un'infrastruttura aggiuntiva** (database H2 in-memory)
2. **Verificano il comportamento a runtime** (diverso dall'analisi statica)
3. **Contribuiscono alla coverage** SonarCloud (JaCoCo)
4. **Hanno scoperto bug reali** che né SonarQube né l'analisi statica potevano rilevare

La separazione permette di:

- **Leggere il report in modo mirato** (chi vuole capire l'analisi statica legge `unit.md`, chi vuole capire il comportamento runtime legge `functional.md`)
- **Documentare in modo pulito** i finding specifici di ciascun approccio
- **Manutenere la documentazione** in modo incrementale

### 8.2 Il valore aggiunto dei test funzionali H2

I test funzionali H2 hanno scoperto **2 bug reali**:

1. **SEC-DAO-01** — `doRetriveBySearch` non case-insensitive (CWE-178)
2. **SEC-IV-03** — Doppio forward in `FiltraggioServletPrezzo` (CWE-754)

Nessuno dei due era rilevabile tramite:

- **SonarQube** (analizza il bytecode, non il comportamento)
- **Analisi statica del sorgente** (verifica pattern testuali)
- **Test unitari Mockito** (mock il DB, non esegue query reali)

Questo dimostra il **valore aggiunto** dell'approccio funzionale nel colmare il gap tra analisi statica e verifica comportamentale.