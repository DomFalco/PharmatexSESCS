-- =====================================================================
-- Schema H2 in-memory per i test funzionali dei DAO.
-- Adattato da Pharmatex.sql per compatibilità con H2 in MODE=MySQL.
--
-- Regole di adattamento:
--   - Rimosse le istruzioni "drop database", "create database", "use"
--     (H2 in-memory non le gestisce).
--   - Sostituito "drop table if exists" per ogni tabella (pulizia).
--   - Corretto il tipo di Acquirente.emailCliente: char(50) -> varchar(50)
--     per matchare Cliente.email (varchar(50)).
--   - Rimossi gli INSERT di esempio (i dati vengono inseriti dai singoli test).
-- =====================================================================

DROP TABLE IF EXISTS Acquistare;
DROP TABLE IF EXISTS CartaDiCredito;
DROP TABLE IF EXISTS Prodotto;
DROP TABLE IF EXISTS Cliente;

-- =====================================================================
-- Tabella Cliente
-- =====================================================================
CREATE TABLE Cliente (
                         email            varchar(50)  PRIMARY KEY,
                         passwordEmail    varchar(100) NOT NULL,
                         nome             varchar(30)  NOT NULL,
                         cognome          varchar(30)  NOT NULL,
                         dataDiNascita    char(10)     NOT NULL,
                         numeroTelefono   char(10)     NOT NULL,
                         codiceFiscale    char(16)     NOT NULL,
                         via              varchar(300) NOT NULL,
                         citta            varchar(50)  NOT NULL,
                         cap              char(5)      NOT NULL,
                         provincia        char(2)      NOT NULL,
                         nazione          char(20)     NOT NULL,
                         amministratore   boolean
);

-- =====================================================================
-- Tabella Prodotto
-- =====================================================================
CREATE TABLE Prodotto (
                          idProdotto              char(5)     PRIMARY KEY,
                          nomeCategoria           varchar(30),
                          nomeProd                varchar(50) NOT NULL,
                          descrizione             text        NOT NULL,
                          larghezza               double,
                          lunghezza               double,
                          prezzo                  double      NOT NULL,
                          quantita                int         NOT NULL,
                          tipoMaterialeMaterasso  varchar(30),
                          coloreLetto             varchar(30),
                          materialeRete           varchar(30),
                          rivestimentoDivano      varchar(30),
                          coloreDivano            varchar(30),
                          tipoStoffaCuscino       varchar(30),
                          materialeCuscino        varchar(30),
                          formaCuscino            varchar(30)
);

-- =====================================================================
-- Tabella Acquistare (FK verso Cliente e Prodotto)
-- =====================================================================
CREATE TABLE Acquistare (
                            emailCliente        varchar(50),
                            idProdotto          char(5),
                            quantitaAcquistata  int,
                            FOREIGN KEY (emailCliente) REFERENCES Cliente(email),
                            FOREIGN KEY (idProdotto)   REFERENCES Prodotto(idProdotto)
);

-- =====================================================================
-- Tabella CartaDiCredito (FK verso Cliente)
-- =====================================================================
CREATE TABLE CartaDiCredito (
                                numeroCarta         varchar(16) PRIMARY KEY,
                                nomeIntestatario    varchar(30) NOT NULL,
                                dataScadenza        char(10)    NOT NULL,
                                CVV                 char(3)     NOT NULL,
                                emailProprietario   varchar(50),
                                FOREIGN KEY (emailProprietario) REFERENCES Cliente(email)
);