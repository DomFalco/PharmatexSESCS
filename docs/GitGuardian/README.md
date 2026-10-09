# GitGuardian — Secret Scanning

**Strumento:** GitGuardian (ggshield)

**Tipo di analisi:** Rilevamento di segreti hardcoded (password, API key, token) nel codice sorgente e nella cronologia Git.

**Data di integrazione:** 28/09/2026

---

## Indice

- [1. Configurazione e Integrazione](#1-configurazione-e-integrazione)
- [2. Processo di Scansione e Rilevamento](#2-processo-di-scansione-e-rilevamento)
    - [Fase 1 — Scansione iniziale (nessun errore rilevato)](#fase-1--scansione-iniziale-nessun-errore-rilevato)
    - [Fase 2 — Test di verifica (modifica temporanea della password)](#fase-2--test-di-verifica-modifica-temporanea-della-password)
    - [Fase 3 — Ripristino e remediation finale](#fase-3--ripristino-e-remediation-finale)
- [3. Vulnerabilità Rilevata e Prioritizzazione](#3-vulnerabilità-rilevata-e-prioritizzazione)
- [4. Dettaglio della Vulnerabilità SEC-01](#4-dettaglio-della-vulnerabilità-sec-01)
- [5. Processo di Remediation](#5-processo-di-remediation)
- [6. Verifica](#6-verifica)

---

## 1. Configurazione e Integrazione

La repository `PharmatexSESCS` è stata collegata alla dashboard di GitGuardian e sottoposta a una scansione iniziale completa. Successivamente, l'integrazione è stata resa automatica tramite GitHub Actions.

- **Workflow:** `.github/workflows/gitguardian.yml`
- **Azione:** `GitGuardian/ggshield-action@v1`
- **Trigger:** Push sui branch `main`/`master` e apertura/sincronizzazione di Pull Request.
- **Autenticazione:** GitHub Actions Secret `GITGUARDIAN_API_KEY` (token con scope minimo `scan`).

Il workflow esegue una scansione ad ogni push, analizzando i nuovi commit e segnalando eventuali segreti introdotti.

---

## 2. Processo di Scansione e Rilevamento

Il processo di identificazione della vulnerabilità si è svolto in tre fasi distinte:

### Fase 1 — Scansione iniziale (nessun errore rilevato)
La repository è stata collegata a GitGuardian e scansionata per la prima volta. In questa fase, **non è stato rilevato alcun incidente**. Questo risultato apparentemente positivo era dovuto al fatto che la password hardcoded rientrava nelle liste di esclusione di GitGuardian, che filtrano i valori a bassa entropia o troppo comuni.

### Fase 2 — Test di verifica (modifica temporanea della password)
Per verificare l'effettiva capacità di rilevamento dello strumento, la password nel file `ConPool.java` è stata temporaneamente modificata con un valore ad **alta entropia**. Questa modifica è stata committata e pushata. GitGuardian ha **immediatamente rilevato la nuova stringa** come potenziale segreto, generando un incidente sulla dashboard.

**Azione:** L'incidente è stato marcato come **ignora** (Test credential) sulla dashboard di GitGuardian, in quanto si trattava di un test controllato per verificare il funzionamento del tool.

### Fase 3 — Ripristino e remediation finale
Dopo aver confermato il funzionamento di GitGuardian, la password originale è stata ripristinata nel codice. Successivamente, è stato eseguito il **fix definitivo**:
- Rimozione della password dal codice sorgente.
- Sostituzione con variabili d'ambiente lette tramite `System.getenv()`.
- Aggiornamento dei file `Dockerfile` e `docker-compose.yml`.

---

## 3. Vulnerabilità Rilevata e Prioritizzazione

La scansione iniziale (Fase 1) non ha prodotto alert. Tuttavia, l'analisi manuale del codice (code review) ha confermato la presenza di una **credenziale hardcoded**, successivamente validata dal test della Fase 2.

| ID | Strumento | File | Categoria (CWE) | Severità (CVSS) | Priorità | Stato | Note |
|:---|:---|:---|:---|:---|:---|:---|:---|
| SEC-01 | GitGuardian | `src/main/java/Model/ConPool.java` | CWE-798: Use of Hard-coded Credentials | 9.8 (Critica) | P0 — Immediata | Risolto | Rilevato dopo modifica temporanea della password ad alta entropia. L'incidente di test è stato marcato come "ignora" per verificarne il funzionamento. |

**Prioritizzazione:** La vulnerabilità è stata classificata come **P0** perché una credenziale in chiaro fornisce accesso diretto al database, con impatto completo su riservatezza, integrità e disponibilità dei dati.

---

## 4. Dettaglio della Vulnerabilità SEC-01

**Descrizione.** La password dell'utente `root` del database MySQL era inserita in chiaro nel codice sorgente (`ConPool.java`), oltre che replicata nei file `Dockerfile` e `docker-compose.yml`.

**Categoria.** CWE-798: Use of Hard-coded Credentials (sotto-categoria CWE-259: Use of Hard-coded Password).

**Impatto.** Chiunque avesse accesso alla repository (o alla sua cronologia Git) avrebbe potuto leggere la password e ottenere accesso completo al database, con possibilità di lettura, modifica e cancellazione dei dati.

**Nota sul rilevamento (falso negativo).** Il valore originale era una stringa a bassa entropia e presente nelle liste di esclusione di GitGuardian, che per policy ignora password comuni o con entropia di Shannon inferiore a 2. Questo spiega perché la scansione iniziale non ha generato alert. La vulnerabilità è stata confermata tramite **code review manuale**, dimostrando che i tool automatici non sostituiscono l'analisi umana.

---

## 5. Processo di Remediation

1. **Rimozione dal codice sorgente.** La password è stata eliminata da [`ConPool.java`](https://github.com/DomFalco/PharmatexSESCS/blob/master/src/main/java/Model/ConPool.java), [`Dockerfile`](https://github.com/DomFalco/PharmatexSESCS/blob/master/Dockerfile) e [`docker-compose.yml`](https://github.com/DomFalco/PharmatexSESCS/blob/master/docker-compose.yaml).
2. **Esternalizzazione della configurazione.** Il codice Java è stato modificato per leggere host, porta, database, utente e password tramite `System.getenv()` (`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`), applicando il principio **fail-fast**: se `MYSQL_PASSWORD` non è impostata, l'applicazione lancia un'eccezione e non parte.
3. **Configurazione locale sicura.** Le variabili sono fornite tramite un file `.env`, aggiunto a `.gitignore` per impedirne il commit.
4. **Configurazione CI/CD sicura.** I valori sono stati salvati come GitHub Actions Secrets (`MYSQL_PASSWORD`, `MYSQL_USER`, `MYSQL_DATABASE`) e iniettati nei job.
5. **Rotazione della credenziale.** La password compromessa è stata cambiata sul database, invalidando il valore esposto nella cronologia Git.
6. **Miglioramento dell'orchestrazione.** È stato aggiunto un `healthcheck` al servizio `db` in Docker Compose, con `depends_on: condition: service_healthy` per il servizio `tomcat`, per garantire che l'applicazione parta solo quando il database è pronto ad accettare connessioni.

---

## 6. Verifica

- **GitHub Actions:** Il workflow viene eseguito automaticamente ad ogni push e pull request. I log dell'ultima esecuzione riportano `No secrets have been found`.
- **Dashboard GitGuardian:** La repository risulta `Monitored`, con `Open incidents: 0` e `Last scan: Successful`.
- **Test funzionale:** L'autenticazione all'applicazione funziona correttamente con le variabili d'ambiente, confermando che la connessione al database avviene tramite la configurazione esternalizzata.