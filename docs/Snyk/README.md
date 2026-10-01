# Snyk — Software Composition Analysis & Static Code Analysis

**Strumento:** Snyk 
**Tipi di analisi:**
- **Snyk Open Source** — analisi delle dipendenze (Maven, Docker) per vulnerabilità note (CVE)
- **Snyk Code** — analisi statica del codice sorgente (SAST) per identificare pattern insicuri

**Data di integrazione:** 01/10/2026

---

# Indice

## 1. Introduzione
### 1.1 Integrazione nella pipeline CI/CD

## 2. Analisi delle dipendenze Maven (`pom.xml`)
### 2.1 Configurazione
### 2.2 Vulnerabilità rilevate
### 2.3 Analisi del rischio
### 2.4 Processo di remediation
#### 2.4.1 Aggiornamento del driver MySQL

## 3. Analisi del Dockerfile
### 3.1 Configurazione iniziale
### 3.2 Prima scansione — 103 vulnerabilità
### 3.3 Processo di remediation
#### 3.3.1 Aggiornamento dei pacchetti di sistema
##### 3.3.1.1 Analisi della CVE High residua: OpenSSL
##### 3.3.1.2 Dettagli della vulnerabilità
##### 3.3.1.3 Perché la CVE è rimasta irrisolta
#### 3.3.2 Passaggio da JDK a JRE
### 3.4 Dopo la remediation
### 3.5 Vulnerabilità residue — Rischio accettato
#### 3.5.1 Motivazione dell'accettazione
### 3.6 Considerazioni finali

## 4. Analisi statica del codice sorgente (Snyk Code)
### 4.1 Configurazione
### 4.2 Vulnerabilità rilevate
### 4.3 Cross-Site Scripting (XSS)
#### 4.3.1 Descrizione
#### 4.3.2 Remediation applicata
#### 4.3.3 Verifica
### 4.4 Trust Boundary Violation
#### 4.4.1 Descrizione
#### 4.4.2 Remediation applicata
#### 4.4.3 Verifica
### 4.5 Use of Password Hash With Insufficient Computational Effort
#### 4.5.1 Descrizione
#### 4.5.2 Decisione: rischio accettato

---
## 1. Introduzione

Snyk è una piattaforma di sicurezza per lo sviluppo software che analizza il codice sorgente, le dipendenze e le immagini container alla ricerca di vulnerabilità note. Snyk si integra nativamente con i repository Git e con le pipeline di CI/CD, consentendo di individuare e correggere i problemi di sicurezza prima che raggiungano la produzione.

### 1.1 Integrazione nella pipeline CI/CD

Snyk è stato integrato nel progetto in due modalità complementari:

1. **Dashboard Snyk** (https://app.snyk.io) — il repository GitHub è stato importato sulla piattaforma per il **monitoraggio continuo**. La dashboard analizza automaticamente il codice ad ogni push e mantiene uno storico delle vulnerabilità nel tempo. Questo permette di individuare nuove CVE che emergono nel tempo anche su codice che non cambia (es. una libreria usata che diventa vulnerabile).

2. **GitHub Actions** — è stata integrata una pipeline di CI/CD che esegue le scansioni Snyk ad ogni push e pull request. La scansione in CI/CD garantisce che:
    - ogni modifica al codice o alle dipendenze sia verificata automaticamente;
    - eventuali nuove vulnerabilità introdotte vengano rilevate prima del merge;
    - la sicurezza sia un requisito continuo e non un controllo una tantum.

---


## 2. Analisi delle dipendenze Maven (`pom.xml`)

### 2.1 Configurazione

Il file [`pom.xml`](../../pom.xml) definisce le dipendenze Maven del progetto. Snyk Open Source ha scansionato le librerie dichiarate e ha rilevato **1 dipendenza vulnerabile** con **6 problemi associati** (2 diretti + 4 transitivi), oltre a una problematica di configurazione (dipendenza duplicata).

### 2.2 Vulnerabilità rilevate

La scheda Snyk della dipendenza `mysql:mysql-connector-java@8.0.18` riporta:

| ID | Categoria (CWE) | Descrizione | Severità | CVSS | Fix disponibile |
|----|-----------------|-------------|----------|------|-----------------|
| SEC-POM-01 | [CWE-611](https://cwe.mitre.org/data/definitions/611.html) | **XML External Entity (XXE) Injection** | Medium | 5.9 | dalla 8.0.27 |
| SEC-POM-02 | [CWE-285](https://cwe.mitre.org/data/definitions/285.html) | **Improper Authorization** | Medium | 6.6 | dalla 8.0.28 |
| SEC-POM-03 | [CWE-696](https://cwe.mitre.org/data/definitions/696.html) | **Denial of Service (DoS)** — ereditata da `com.google.protobuf:protobuf-java@3.6.1` | Medium | 7.5 | dalla 8.0.29 |

**Priority Score della dipendenza:** `649` (massimo 1000).

### 2.3 Analisi del rischio

Le tre CVE sono **High/Medium con fix disponibile** e riguardano una libreria di uso diretto nel codice del progetto (`ConPool.java`). L'impatto potenziale è:

1. **XXE Injection (CWE-611):** un attaccante potrebbe iniettare entità XML esterne per leggere file locali al server o eseguire richieste verso sistemi interni.
2. **Improper Authorization (CWE-285):** un controllo di autorizzazione inadeguato in MySQL Connector potrebbe consentire a un utente di accedere a risorse non autorizzate.
3. **DoS su protobuf-java (CWE-696):** un input craftato nella deserializzazione protobuf potrebbe causare un blocco del servizio.

**Priorità assegnata:** **P0 — Immediata**, perché si tratta di vulnerabilità nella libreria di connessione al database, sotto il controllo diretto del progetto, con fix disponibile e senza impatto sul codice applicativo.

### 2.4 Processo di remediation

#### 2.4.1 Aggiornamento del driver MySQL

L'aggiornamento nel [`pom.xml`](https://github.com/DomFalco/PharmatexSESCS/blob/master/pom.xml#L54-L58) dalla versione **8.0.18** alla versione **8.0.33** di `mysql-connector-java` risolve tutte e tre le CVE (XXE → fixata dalla 8.0.27; Improper Authorization → dalla 8.0.28; DoS transitiva su protobuf → dalla 8.0.29). È stata scelta la 8.0.33 perché è l'ultima versione della serie 8.0.x mantenuta stabile, con **compatibilità garantita** con il codice esistente (`ConPool.java` utilizza la classe `com.mysql.cj.jdbc.Driver`, invariata tra le due versioni).

---

## 3. Analisi del Dockerfile

### 3.1 Configurazione iniziale

Il `Dockerfile` del progetto è di tipo **multi-stage** e utilizza due immagini base:

| Stage | Immagine | Ruolo |
|-------|----------|-------|
| Builder | `maven:3.9.6-eclipse-temurin-17` | Compila il codice sorgente e produce il WAR |
| Runtime | `tomcat:10.1-jdk17` | Esegue l'applicazione nel container finale |

L'analisi Snyk Open Source sul Dockerfile valuta le vulnerabilità ereditate dalle immagini base e dai pacchetti di sistema installati. Viene eseguita automaticamente ad ogni push tramite la dashboard Snyk.

### 3.2 Prima scansione — 103 vulnerabilità

La prima scansione ha rilevato **103 vulnerabilità**, distribuite per severità:

| Severità | Occorrenze |
|----------|-----------|
| Critical | 0 |
| High | 1 |
| **Medium** | **83** |
| Low | 19 |
| **Totale** | **103** |

**Nota sul conteggio:** Snyk conta ogni **percorso di dipendenza**. La stessa CVE (es. `CVE-2026-84782` su openssl) appare più volte perché ereditata da più sub-pacchetti (`libssl3t64`, `ca-certificates`, `curl`, `krb5`, `openldap`, ecc.). Il numero **27** rappresenta le CVE distinte (uniche) del runtime, mentre 103 è il numero totale di occorrenze.

### 3.3 Processo di remediation

Sono state applicate **due modifiche** al `Dockerfile`, entrambe sotto il controllo diretto del progetto.

#### 3.3.1 Aggiornamento dei pacchetti di sistema

In entrambi gli stage (builder e runtime) è stata aggiunta la riga:

```dockerfile
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
```

Questa istruzione forza l'aggiornamento di tutti i pacchetti di sistema installati nell'immagine alla versione più recente disponibile nel repository Ubuntu.

##### 3.3.1.1 Analisi della CVE High residua: OpenSSL

La vulnerabilità di severità **High** identificata da Snyk è **CVE-2026-84782** nel pacchetto `openssl`.

##### 3.3.1.2 Dettagli della vulnerabilità

| Campo | Valore                                                                |
| :--- |:----------------------------------------------------------------------|
| **CVE** | CVE-2026-84782                                                        |
| **SNYK ID** | SNYK-UBUNTU2204-OPENSSL-20266990                                      |
| **Pacchetto** | `openssl@3.0.2-0ubuntu1.26`, `openssl/libssl3@3.0.2-0ubuntu1.26`      |
| **Severità** | High (Snyk: High, Ubuntu Security Rating: High, Red Hat: Important) |
| **CVSS v3** | 7.4 (Red Hat), 8.2 (cve.org)                                          |
| **CWE** | CWE-125: Out-of-bounds Read                                           |
| **Exploit maturity** | No known exploit                                                      |
| **Fixed in** | `openssl@3.0.2-0ubuntu1.30` (per Ubuntu 22.04)                        |

**Descrizione tecnica:** la vulnerabilità risiede nella logica di ritrasmissione del protocollo **DTLS** (Datagram Transport Layer Security). Quando un messaggio di handshake viene scritto in modo frammentato e la scrittura viene sospesa (a causa di un buffer di trasporto momentaneamente pieno), il timer di ritrasmissione DTLS può erroneamente riutilizzare lo stesso buffer e la stessa posizione di tracking della scrittura sospesa, senza resettare la posizione all'inizio del messaggio. Ciò può causare una lettura oltre i limiti del buffer (out-of-bounds read), con potenziale **divulgazione di memoria heap** al peer come dati di handshake in chiaro, oppure un **crash del processo** (Denial of Service) se la lettura raggiunge una regione di memoria non mappata.

##### 3.3.1.3 Perché la CVE è rimasta irrisolta

La vulnerabilità è stata **accettata come rischio residuo** per le seguenti motivazioni tecniche:

1. **Dipendenza dalla base image upstream:** la CVE è ereditata dalla base image `tomcat:10.1-jre17-temurin-jammy`. Snyk stesso conferma che **"The base image tomcat:10.1-jre17-temurin-jammy is up to date"**, ovvero non esiste una versione più recente di questa immagine che risolva la vulnerabilità.

2. **Patch non disponibile nei repository Ubuntu 22.04:** sebbene OpenSSL abbia rilasciato le patch nelle versioni 4.0.3, 3.6.5, 3.5.9 e 3.4.8 per i branch mantenuti, la versione 3.0 (utilizzata in Ubuntu 22.04) **ha raggiunto il suo end-of-life il 7 settembre 2026** e non riceve più aggiornamenti di sicurezza pubblici. Le correzioni per il branch 3.0 (versione 3.0.23) sono disponibili **solo per i clienti con supporto premium** di OpenSSL. Di conseguenza, la versione `openssl@3.0.2-0ubuntu1.30` (indicata come "Fixed in" da Snyk) non è ancora stata rilasciata nei repository pubblici di Ubuntu 22.04.

3. **Impatto limitato al protocollo DTLS:** la vulnerabilità **riguarda esclusivamente il protocollo DTLS su UDP**. Le connessioni TLS standard su TCP, che rappresentano il modello di deployment tipico per la maggior parte delle applicazioni web (incluso Tomcat), **non sono interessate**. L'applicazione `Pharmatex` utilizza Tomcat come servlet container e non fa uso di DTLS, il che riduce drasticamente la probabilità di sfruttamento.

4. **Exploit maturity "No known exploit":** Snyk classifica la vulnerabilità con **"No known exploit"**, indicando che non esiste un exploit pubblico funzionante al momento dell'analisi.

#### 3.3.2 Passaggio da JDK a JRE

L'immagine runtime è stata cambiata da una basata su **JDK** a una basata su **JRE**:

| | Prima | Dopo |
|---|-------|------|
| Stage runtime | `tomcat:10.1-jdk17` | `tomcat:10.1-jre17-temurin-jammy` |
| Contenuto | Java Development Kit completo | Java Runtime Environment |

**Motivazione:** il JDK include strumenti di sviluppo non necessari a runtime (compilatore `javac`, linker, `binutils`, `gcc-12` e relative librerie). L'applicazione è già compilata in un WAR durante lo stage builder, quindi a runtime serve solo il JRE per eseguire il bytecode.

Questa modifica applica il principio di **"least functionality"** (minima funzionalità): ridurre i componenti installati riduce la superficie d'attacco.

Il builder continua a usare il JDK (necessario per la compilazione Maven).

### 3.4 Dopo la remediation

Dopo le modifiche, la dashboard Snyk riporta:

| Severità | Prima | Dopo | Variazione |
|----------|-------|------|------------|
| Critical | 0 | 0 | — |
| High | 1 | 1 | — |
| **Medium** | **83** | **68** | **~15** |
| Low | 19 | 20 | +1 |
| **Totale** | **103** | **89** | **~14** |

**Riduzione complessiva:** ~14 vulnerabilità totali, di cui **~15 Medium**.

**CVE eliminate dal passaggio a JRE:** principalmente quelle relative al pacchetto `binutils` e alle librerie di sviluppo incluse nel JDK (`gcc-12`, `libstdc++6`, `binutils-common`, `libbinutils`, `libctf0`). Il JRE non le include, quindi le relative occorrenze sono scomparse dalla dashboard.

### 3.5 Vulnerabilità residue — Rischio accettato

Le **89 vulnerabilità residue** sono tutte nei pacchetti di sistema Ubuntu dell'immagine base `tomcat:10.1-jre17-temurin-jammy`. Snyk conferma:

> *"According to our scan, you are currently using the most secure version of the selected base image."*

**Non esiste una versione più sicura del tag `tomcat:10.1-jre17-temurin-jammy`.**

#### 3.5.1 Motivazione dell'accettazione

Le vulnerabilità residue sono state accettate come **rischio residuo controllato**, in base ai seguenti criteri:

1. **Origine upstream**: sono ereditate dai pacchetti di Ubuntu 22.04 inclusi nell'immagine base, non introdotte dal codice del progetto.
2. **No fix disponibile**: per la maggior parte di esse Snyk riporta `Fixed in: []`, ovvero Ubuntu non ha ancora rilasciato una patch. Aggiornare il tag dell'immagine non risolverebbe la situazione.
3. **Contesto di deployment**: l'applicazione è eseguita in un contesto didattico, non esposta su rete pubblica.
4. **Exploit complesso**: le CVE residue richiedono in genere accesso locale o input controllato per essere sfruttate (come confermato dal campo *"Exploit maturity: NO KNOWN EXPLOIT"* in quasi tutte le schede).
5. **Cambiare immagine non risolve**: testare varianti alternative di Ubuntu (Noble, Jammy) o Tomcat 11 ha evidenziato un **aumento** delle CVE, non una riduzione.

### 3.6 Considerazioni finali

L'analisi del [`Dockerfile`](https://github.com/DomFalco/PharmatexSESCS/blob/master/Dockerfile#L12) ha permesso di applicare due miglioramenti concreti:
1. **Aggiornamento dei pacchetti di sistema** tramite `apt-get upgrade`: risolve le vulnerabilità fixabili upstream e garantisce che il container usi le versioni più recenti disponibili per Ubuntu 22.04.
2. **Riduzione della superficie d'attacco** tramite JRE invece di JDK: elimina 15 CVE Medium legate a `binutils` e ai tool di sviluppo, senza impatti funzionali.

Le 89 vulnerabilità residue non sono risolvibili dal progetto: dipendono da pacchetti upstream di Ubuntu per cui non esiste ancora una patch. Sono state pertanto **accettate come rischio residuo controllato**.

---
## 4. Analisi statica del codice sorgente (Snyk Code)

### 4.1 Configurazione

Snyk Code è il motore di analisi statica del codice (SAST) di Snyk. Analizza il codice sorgente Java e JSP del progetto alla ricerca di pattern insicuri — input non validati, output non sanitizzato, uso di algoritmi crittografici deboli, gestione errata delle sessioni — **senza eseguire il codice**.

Snyk Code ha rilevato **8 problemi** nel codice del progetto: **7 risolti** e **1 accettato come rischio residuo**.

### 4.2 Vulnerabilità rilevate

| ID | Categoria (CWE) | File | Severità | Score | Stato |
|----|-----------------|------|----------|-------|-------|
| SEC-C01 | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) — Cross-Site Scripting (XSS) | `Prodotti.jsp` | High | 650 | Risolto |
| SEC-C02 | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) — Cross-Site Scripting (XSS) | `Prodotti.jsp` | High | 650 | Risolto |
| SEC-C03 | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) — Cross-Site Scripting (XSS) | `Prodotti.jsp` | High | 650 | Risolto |
| SEC-C04 | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) — Cross-Site Scripting (XSS) | `Prodotti.jsp` | High | 650 | Risolto |
| SEC-C05 | [CWE-501](https://cwe.mitre.org/data/definitions/501.html) — Trust Boundary Violation | `InizioServlet.java` | Low | 225 | Risolto |
| SEC-C06 | [CWE-501](https://cwe.mitre.org/data/definitions/501.html) — Trust Boundary Violation | `FiltraggioServletMateriale.java` | Low | 275 | Risolto |
| SEC-C07 | [CWE-501](https://cwe.mitre.org/data/definitions/501.html) — Trust Boundary Violation | `FiltraggioServletMateriale.java` | Low | 275 | Risolto |
| SEC-C08 | [CWE-916](https://cwe.mitre.org/data/definitions/916.html) — Use of Password Hash With Insufficient Computational Effort | `Utente.java` | Low | 175 | Rischio accettato |


### 4.3 Cross-Site Scripting (XSS)

#### 4.3.1 Descrizione

**Categoria:** [CWE-79: Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')](https://cwe.mitre.org/data/definitions/79.html)

Durante l'analisi statica, Snyk Code ha rilevato **4 occorrenze di Cross-Site Scripting (XSS)** nel file `src/main/webapp/WEB-INF/results/Prodotti.jsp`, classificate con severità **High** (score 650). Il problema era causato dall'output diretto di parametri HTTP (`request.getParameter`) e attributi di richiesta (`request.getAttribute`) all'interno del codice JSP tramite scriptlet `<%= ... %>`, senza alcuna forma di escaping.

Le 4 occorrenze corrispondono a:

1. **Titolo della pagina**: `<title><%=x%></title>` — `x` deriva da `request.getParameter("action")` o `request.getAttribute("filtraggio")`.
2. **Attributo `href` del link**: `<a href="RicercaServlet?search=<%=p.getNomeProd()%>">` — `p.getNomeProd()` è un dato proveniente dal database, potenzialmente manipolabile.
3. **Attributo `src` dell'immagine**: `<img src="<%=directory%>">` — `directory` è una stringa costruita a partire da `p.getIdProdotto()`.
4. **Contenuto testuale del modello**: `<b style="text-align: center;">Modello:<%=p.getNomeProd()%></b>`.

**Impatto potenziale:** un attaccante potrebbe iniettare codice JavaScript malevolo che verrebbe eseguito nel browser della vittima, con possibilità di rubare cookie di sessione, dirottare l'utente su siti malevoli o manipolare il DOM della pagina.

#### 4.3.2 Remediation applicata

Per risolvere le vulnerabilità sono state adottate **due misure complementari**:

1. **Aggiornamento delle dipendenze Maven [`pom.xml`](https://github.com/DomFalco/PharmatexSESCS/blob/master/pom.xml#L28-L38)** Sono state aggiunte le dipendenze **JSTL (Jakarta Standard Tag Library)**.
2. **Nel file [`Prodotti.jsp`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/webapp/WEB-INF/results/Prodotti.jsp#L5) è stata inoltre aggiornata la direttiva taglib alla nuova URI Jakarta.

Gli scriptlet `<%= ... %>` sono stati sostituiti con il tag `<c:out>`, che esegue automaticamente l'escaping dei caratteri HTML speciali (`<`, `>`, `"`, `'`, `&`), neutralizzando qualsiasi tentativo di iniezione di script.

| Riga | Prima | Dopo |
|------|-------|------|
| 21 | `<title><%=x%></title>` | `<title><c:out value="<%=x%>"/></title>` 🔗 [Vedi la riga 21](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/webapp/WEB-INF/results/Prodotti.jsp#L22)|
| 38 | `<a href="RicercaServlet?search=<%=p.getNomeProd()%>">` | `<a href="RicercaServlet?search=<c:out value='<%=p.getNomeProd()%>'/>">` 🔗 [Vedi la riga 38](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/webapp/WEB-INF/results/Prodotti.jsp#L38)|
| 39 | `<img src="<%=directory%>">` | `<img src="<c:out value='<%=directory%>'/>">` 🔗 [Vedi la riga 39](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/webapp/WEB-INF/results/Prodotti.jsp#L39)|
| 43 | `Modello:<%=p.getNomeProd()%>` | `Modello:<c:out value="<%=p.getNomeProd()%>"/>`  🔗 [Vedi la riga 43](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/webapp/WEB-INF/results/Prodotti.jsp#L43)|

#### 4.3.3 Verifica

Dopo il rescan di Snyk Code, le **4 occorrenze** di XSS non compaiono più nel report. Il file `Prodotti.jsp` risulta pulito e l'applicazione visualizza correttamente i contenuti senza interpretare input malevoli.

### 4.4 Trust Boundary Violation

#### 4.4.1 Descrizione

**Categoria:** [CWE-501: Trust Boundary Violation](https://cwe.mitre.org/data/definitions/501.html)

Il problema si verifica quando **input proveniente da una fonte non affidabile** (parametri HTTP inviati dal client tramite `request.getParameter()`) viene **inserito direttamente in una struttura dati considerata affidabile** (la sessione HTTP) senza essere prima validato o sanitizzato.

**Impatto potenziale:**
- **Cross-Site Scripting (XSS)** se il valore della sessione viene renderizzato in una pagina HTML senza escaping.
- **HTTP Parameter Pollution** se altri servlet si fidano ciecamente del valore in sessione.
- **Manipolazione della logica applicativa** se il valore in sessione viene usato come parametro di controllo.

#### 4.4.2 Remediation applicata

Le **3 vulnerabilità** rilevate sono state risolte introducendo opportune misure di sicurezza sui parametri HTTP prima del loro utilizzo.

**In `FiltraggioServletMateriale.java`:**

1. **Null check** — verifica esplicita che i parametri `mat` e `materiale` non siano `null`, evitando NullPointerException. 🔗 [Vedi le righe 19-21](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/FiltraggioServletMateriale.java#L19-L21)
2. **Sanitizzazione con whitelist** — rimozione di tutti i caratteri non alfanumerici tramite `replaceAll("[^a-zA-Z0-9\\s]", "")`, creando le variabili sicure `matSicuro` e `materialeSicuro`. 🔗 [Vedi le righe 23-26](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/FiltraggioServletMateriale.java#L23-L26)
3. **Inserimento sicuro in sessione** — uso delle variabili sanitizzate (`matSicuro`, `materialeSicuro`) in `setAttribute`, invece degli input originali. 🔗 [Vedi le righe 46-50](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/FiltraggioServletMateriale.java#L46-L50)

**In `InizioServlet.java`:**

1. **Null check e validazione di `valore`** — se `richiesta` è `null`, il parametro `valore` viene validato con regex `matches("[a-zA-Z0-9\\s]+")`. Se non conforme, `sendError(SC_BAD_REQUEST)`. 🔗 [Vedi le righe 21-43](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/InizioServlet.java#L21-L43)
2. **Validazione di `richiesta`** — verifica con la stessa regex prima di qualsiasi utilizzo, con `sendError(SC_BAD_REQUEST)` se non conforme. 🔗 [Vedi le righe 45-49](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/InizioServlet.java#L45-L49)
3. **Inserimento sicuro in sessione** — `setAttribute` avviene solo dopo la validazione, quindi il valore è ormai sicuro. 🔗 [Vedi le righe 64-66](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Controller/InizioServlet.java#L64-L66)

#### 4.4.3 Verifica

Dopo il rescan di Snyk Code, le **3 occorrenze** di Trust Boundary Violation non compaiono più nel report. Il file `InizioServlet.java` risulta pulito e `FiltraggioServletMateriale.java` non contiene più i due alert originali.

### 4.5 Use of Password Hash With Insufficient Computational Effort

#### 4.5.1 Descrizione

**Categoria:** [CWE-916: Use of Password Hash With Insufficient Computational Effort](https://cwe.mitre.org/data/definitions/916.html)

Il metodo `setPassword()` in [`Utente.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/Utente.java#L70) utilizza **SHA-1** tramite `MessageDigest.getInstance("SHA-1")` per hashare le password degli utenti.

**SHA-1 è inadatto all'hashing di password** perché:

1. È un algoritmo **veloce**: un PC moderno può calcolare milioni di hash SHA-1 al secondo.
2. **Non usa salt**: due utenti con la stessa password producono lo stesso hash.
3. È **crittograficamente debole** dal 2017 (attacco SHAttered di Google).
4. Un attaccante con accesso al database può eseguire un attacco a dizionario o brute-force in tempi molto brevi.

#### 4.5.2 Decisione: rischio accettato

La vulnerabilità è stata **accettata come rischio residuo controllato**, non risolta.

**Motivazione:**

Il fix corretto richiede un refactoring strutturale che coinvolge più componenti del progetto:

1. **Modifica del codice Java** — introdurre una libreria di hashing sicura (BCrypt, Argon2id o PBKDF2).
2. **Modifica dello schema database** — la colonna `passwordEmail` deve essere estesa a `varchar(60)` (dimensione di un hash BCrypt).
3. **Rigenerazione degli hash esistenti** — tutti i record in `Pharmatex.sql` vanno aggiornati con hash BCrypt generati a partire dalle password originali.
4. **Aggiornamento del flusso di login** — sostituire il confronto tra hash SHA-1 con il metodo `BCrypt.checkpw()`.
5. **Test end-to-end** — verificare tutti i flussi di autenticazione.

