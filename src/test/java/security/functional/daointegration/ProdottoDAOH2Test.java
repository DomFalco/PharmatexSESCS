package security.functional.daointegration;

import model.Prodotto;
import model.ProdottoDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseH2Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali del ProdottoDAO su H2 in-memory.
 */
class ProdottoDAOH2Test extends BaseH2Test {

    @Test
    @DisplayName("doRetriveAll restituisce tutti i prodotti presenti nel DB")
    void testDoRetriveAll() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));

        List<Prodotto> result = ProdottoDAO.doRetriveAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0002");
    }

    @Test
    @DisplayName("doRetriveAll su DB vuoto restituisce lista vuota")
    void testDoRetriveAllEmpty() {
        List<Prodotto> result = ProdottoDAO.doRetriveAll();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("doRetriveByCategoria filtra correttamente per categoria")
    void testDoRetriveByCategoria() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));
        executeSql(insertProdotto("P0003", "Materasso", "Roma",   239.0, 5));

        List<Prodotto> result = ProdottoDAO.doRetriveByCategoria("Materasso");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getNomeCategoria)
                .containsOnly("Materasso");
    }

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

        List<Prodotto> result = ProdottoDAO.doRetriveByFilter("Materasso", 450.0, 250.0);

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

        List<Prodotto> result = ProdottoDAO.doRetriveQuantitaEsaurita();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdProdotto()).isEqualTo("P0002");
    }

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

        List<Prodotto> all = ProdottoDAO.doRetriveAll();
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

        List<Prodotto> all = ProdottoDAO.doRetriveAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getQuantita()).isEqualTo(2);
    }

    @Test
    @DisplayName("doSetNewPrezzo aggiorna il prezzo del prodotto")
    void testDoSetNewPrezzo() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        ProdottoDAO.doSetNewPrezzo(299.99, "P0001");

        List<Prodotto> all = ProdottoDAO.doRetriveAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getPrezzo()).isEqualTo(299.99);
    }

    @Test
    @DisplayName("doRetriveMaterialeMaterasso filtra per tipo di materiale del materasso")
    void testDoRetriveMaterialeMaterasso() throws Exception {
        executeSql(insertProdottoCompleto("P0001", "Materasso", "Nuvola",
                400.0, 5, "Memory", null, null));
        executeSql(insertProdottoCompleto("P0002", "Materasso", "Roma",
                239.0, 5, "Lattice", null, null));
        executeSql(insertProdottoCompleto("P0003", "Materasso", "Giglio",
                350.0, 5, "Memory", null, null));

        List<Prodotto> result = ProdottoDAO.doRetriveMaterialeMaterasso("Memory");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0003");
    }

    @Test
    @DisplayName("doRetriveMaterialeRete filtra per materiale della rete")
    void testDoRetriveMaterialeRete() throws Exception {
        executeSql(insertProdottoCompleto("R0001", "Rete", "Faggio",
                570.0, 5, null, "Faggio", null));
        executeSql(insertProdottoCompleto("R0002", "Rete", "Tecna",
                530.0, 5, null, "Ferro", null));
        executeSql(insertProdottoCompleto("R0003", "Rete", "GoldSoft",
                329.0, 5, null, "Faggio", null));

        List<Prodotto> result = ProdottoDAO.doRetriveMaterialeRete("Faggio");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("R0001", "R0003");
    }

    @Test
    @DisplayName("doRetriveMaterialeCuscino filtra per materiale del cuscino")
    void testDoRetriveMaterialeCuscino() throws Exception {
        executeSql(insertProdottoCompleto("C0001", "Cuscino", "Greta",
                40.0, 5, null, null, "Memory"));
        executeSql(insertProdottoCompleto("C0002", "Cuscino", "Guanciale",
                30.0, 5, null, null, "Poliestere"));
        executeSql(insertProdottoCompleto("C0003", "Cuscino", "Salute",
                40.0, 5, null, null, "Memory"));

        List<Prodotto> result = ProdottoDAO.doRetriveMaterialeCuscino("Memory");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Prodotto::getIdProdotto)
                .containsExactlyInAnyOrder("C0001", "C0003");
    }

    @Test
    @DisplayName("SQL Injection su doRetriveBySearch viene neutralizzata dal PreparedStatement")
    void testSqlInjectionNeutralizzata() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        Prodotto result = ProdottoDAO.doRetriveBySearch("' OR '1'='1");

        assertThat(result.getIdProdotto()).isNull();
    }

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }

    private String insertProdottoCompleto(String id, String categoria, String nome,
                                          double prezzo, int quantita,
                                          String materialeMaterasso,
                                          String materialeRete,
                                          String materialeCuscino) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita, tipoMaterialeMaterasso, materialeRete, materialeCuscino) VALUES ("
                + "'" + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ", "
                + (materialeMaterasso == null ? "NULL" : "'" + materialeMaterasso + "'") + ", "
                + (materialeRete == null ? "NULL" : "'" + materialeRete + "'") + ", "
                + (materialeCuscino == null ? "NULL" : "'" + materialeCuscino + "'")
                + ")";
    }
}