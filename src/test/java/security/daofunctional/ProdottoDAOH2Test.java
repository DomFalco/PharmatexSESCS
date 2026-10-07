package security.daofunctional;

import Model.Prodotto;
import Model.ProdottoDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali del ProdottoDAO su H2 in-memory.
 * Verificano il comportamento runtime delle query SQL, cosa che l'analisi
 * statica del sorgente non puo' fare. Ogni test popola il DB con dati
 * controllati, chiama il metodo del DAO e verifica il risultato.
 */
class ProdottoDAOH2Test extends BaseH2Test {

    // ==================================================================
    // Test di lettura (SELECT)
    // ==================================================================

    @Test
    @DisplayName("doRetriveAll restituisce tutti i prodotti presenti nel DB")
    void testDoRetriveAll() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));

        ArrayList<Prodotto> result = ProdottoDAO.doRetriveAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0002");
    }

    @Test
    @DisplayName("doRetriveAll su DB vuoto restituisce lista vuota")
    void testDoRetriveAllEmpty() {
        ArrayList<Prodotto> result = ProdottoDAO.doRetriveAll();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("doRetriveByCategoria filtra correttamente per categoria")
    void testDoRetriveByCategoria() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));
        executeSql(insertProdotto("P0003", "Materasso", "Roma",   239.0, 5));

        ArrayList<Prodotto> result = ProdottoDAO.doRetriveByCategoria("Materasso");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getNomeCategoria)
                .containsOnly("Materasso");
    }

    /**
     * Verifica del fix SEC-DAO-01.
     * Prima del fix: doRetriveBySearch("nuvola") restituiva null perche' il
     * pattern non era uppercasato lato Java, nonostante la query usasse
     * upper(nomeProd). Il bug e' stato scoperto SOLO grazie a questo test
     * funzionale su H2.
     */
    @Test
    @DisplayName("FIX SEC-DAO-01: doRetriveBySearch e' case-insensitive (fix applicato)")
    void testDoRetriveBySearch_CaseInsensitive() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        Prodotto lowercase = ProdottoDAO.doRetriveBySearch("nuvola");
        assertThat(lowercase.getIdProdotto()).isEqualTo("P0001");

        Prodotto uppercase = ProdottoDAO.doRetriveBySearch("NUVOLA");
        assertThat(uppercase.getIdProdotto()).isEqualTo("P0001");

        Prodotto mixedcase = ProdottoDAO.doRetriveBySearch("NuVoLa");
        assertThat(mixedcase.getIdProdotto()).isEqualTo("P0001");
    }

    @Test
    @DisplayName("doRetriveByFilter filtra per categoria e range di prezzo")
    void testDoRetriveByFilter() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma",   239.0, 5));
        executeSql(insertProdotto("P0003", "Materasso", "Giglio", 350.0, 5));

        ArrayList<Prodotto> result = ProdottoDAO.doRetriveByFilter("Materasso", 450.0, 250.0);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0003");
    }

    @Test
    @DisplayName("doRetriveQuantitaEsaurita restituisce solo prodotti con quantita=0")
    void testDoRetriveQuantitaEsaurita() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma",   239.0, 0));

        ArrayList<Prodotto> result = ProdottoDAO.doRetriveQuantitaEsaurita();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdProdotto()).isEqualTo("P0002");
    }

    // ==================================================================
    // Test di scrittura (INSERT / UPDATE / DELETE)
    // ==================================================================

    @Test
    @DisplayName("aggiuntaProdotto inserisce correttamente un nuovo prodotto")
    void testAggiuntaProdotto() {
        Prodotto p = new Prodotto();
        p.setIdProdotto("P0099");
        p.setNomeCategoria("Cuscino");
        p.setNomeProd("TestCuscino");
        p.setDescrizione("Descrizione di test");
        p.setLarghezza(70.0);
        p.setLunghezza(45.0);
        p.setPrezzo(40.0);
        p.setQuantita(10);

        ProdottoDAO.aggiuntaProdotto(p);

        ArrayList<Prodotto> all = ProdottoDAO.doRetriveAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getIdProdotto()).isEqualTo("P0099");
        assertThat(all.get(0).getNomeProd()).isEqualTo("TestCuscino");
        assertThat(all.get(0).getPrezzo()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("cancellaProdotto rimuove correttamente il prodotto")
    void testCancellaProdotto() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        ProdottoDAO.cancellaProdotto("P0001");

        assertThat(ProdottoDAO.doRetriveAll()).isEmpty();
    }

    @Test
    @DisplayName("doUpdateQuantita aggiorna la quantita del prodotto")
    void testDoUpdateQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        ProdottoDAO.doUpdateQuantita(2, "P0001");

        ArrayList<Prodotto> all = ProdottoDAO.doRetriveAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getQuantita()).isEqualTo(2);
    }

    @Test
    @DisplayName("doSetNewPrezzo aggiorna il prezzo del prodotto")
    void testDoSetNewPrezzo() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        ProdottoDAO.doSetNewPrezzo(299.99, "P0001");

        ArrayList<Prodotto> all = ProdottoDAO.doRetriveAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getPrezzo()).isEqualTo(299.99);
    }

    // ==================================================================
    // Test di sicurezza (SQL Injection a runtime)
    // ==================================================================

    @Test
    @DisplayName("SQL Injection su doRetriveBySearch viene neutralizzata dal PreparedStatement")
    void testSqlInjectionNeutralizzata() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        Prodotto result = ProdottoDAO.doRetriveBySearch("' OR '1'='1");

        assertThat(result.getIdProdotto()).isNull();
    }

    // ==================================================================
    // Helper: INSERT di un prodotto con i campi minimi
    // ==================================================================

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}