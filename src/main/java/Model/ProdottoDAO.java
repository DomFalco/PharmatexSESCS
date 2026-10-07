package Model;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "ProdottoDAO", value = "/ProdottoDAO")
public class ProdottoDAO extends HttpServlet {
    public static ArrayList<Prodotto> doRetriveByCategoria(String categoria) {
        ArrayList<Prodotto> elencoProdotti = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE nomeCategoria LIKE ?")) {
            ps.setString(1, categoria.concat("%"));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                    elencoProdotti.add(p);
                }
            }
            return elencoProdotti;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveByCategoria", e);
        }
    }


    public static ArrayList<Prodotto> doRetriveAll() {
        ArrayList<Prodotto> elencoProdotti = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prodotto p = new Prodotto();
                p.setIdProdotto(rs.getString(1));
                p.setNomeCategoria(rs.getString(2));
                p.setNomeProd(rs.getString(3));
                p.setDescrizione(rs.getString(4));
                p.setLarghezza(rs.getDouble(5));
                p.setLunghezza(rs.getDouble(6));
                p.setPrezzo(rs.getDouble(7));
                p.setQuantita(rs.getInt(8));
                p.setTipoMaterialeMaterasso(rs.getString(9));
                p.setColoreLetto(rs.getString(10));
                p.setMaterialeRete(rs.getString(11));
                p.setRivestimentoDivano(rs.getString(12));
                p.setColoreDivano(rs.getString(13));
                p.setTipoStoffaCuscino(rs.getString(14));
                p.setMaterialeCuscino(rs.getString(15));
                p.setFormaCuscino(rs.getString(16));
                elencoProdotti.add(p);
            }
            return elencoProdotti;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveAll", e);
        }
    }

    public static ArrayList<Prodotto> doRetriveByFilter(String categoria, double max, double min) {
        ArrayList<Prodotto> elencoLetti = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE nomeCategoria LIKE ? AND prezzo>= ? AND prezzo<= ?")) {
            ps.setString(1, categoria.substring(0, categoria.length() - 1).concat("%"));
            ps.setDouble(2, min);
            ps.setDouble(3, max);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                    elencoLetti.add(p);
                }
            }
            return elencoLetti;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveByFilter", e);
        }
    }

    public static Prodotto doRetriveBySearch(String nome) {
        Prodotto p = new Prodotto();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE upper(nomeProd) LIKE upper(?)")) {
            ps.setString(1, nome.concat("%"));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                }
            }
            return p;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveBySearch", e);
        }
    }

    public static void doUpdateQuantita(int q, String idProdotto) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Prodotto SET quantita=? WHERE idProdotto=?",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q);
            ps.setString(2, idProdotto);
            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("UPDATE error.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doUpdateQuantita", e);
        }
    }

    public static void doSetNewPrezzo(double prezzo, String idProdotto) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Prodotto SET prezzo=? WHERE idProdotto=?",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, prezzo);
            ps.setString(2, idProdotto);
            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("UPDATE error.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doSetNewPrezzo", e);
        }
    }

    public static ArrayList<Prodotto> doRetriveMaterialeMaterasso(String materiale) {
        ArrayList<Prodotto> elencoMaterassi = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE tipoMaterialeMaterasso=?")) {
            ps.setString(1, materiale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                    elencoMaterassi.add(p);
                }
            }
            return elencoMaterassi;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveMaterialeMaterasso", e);
        }
    }

    public static ArrayList<Prodotto> doRetriveMaterialeRete(String materiale) {
        ArrayList<Prodotto> elencoReti = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE materialeRete=?")) {
            ps.setString(1, materiale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                    elencoReti.add(p);
                }
            }
            return elencoReti;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveMaterialeRete", e);
        }
    }

    public static ArrayList<Prodotto> doRetriveMaterialeCuscino(String materiale) {
        ArrayList<Prodotto> elencoCuscini = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE materialeCuscino=?")) {
            ps.setString(1, materiale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getString(1));
                    p.setNomeCategoria(rs.getString(2));
                    p.setNomeProd(rs.getString(3));
                    p.setDescrizione(rs.getString(4));
                    p.setLarghezza(rs.getDouble(5));
                    p.setLunghezza(rs.getDouble(6));
                    p.setPrezzo(rs.getDouble(7));
                    p.setQuantita(rs.getInt(8));
                    p.setTipoMaterialeMaterasso(rs.getString(9));
                    p.setColoreLetto(rs.getString(10));
                    p.setMaterialeRete(rs.getString(11));
                    p.setRivestimentoDivano(rs.getString(12));
                    p.setColoreDivano(rs.getString(13));
                    p.setTipoStoffaCuscino(rs.getString(14));
                    p.setMaterialeCuscino(rs.getString(15));
                    p.setFormaCuscino(rs.getString(16));
                    elencoCuscini.add(p);
                }
            }
            return elencoCuscini;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveMaterialeCuscino", e);
        }
    }

    public static ArrayList<Prodotto> doRetriveQuantitaEsaurita() {
        ArrayList<Prodotto> prodottiEsauriti = new ArrayList<Prodotto>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idProdotto, nomeCategoria,nomeProd,descrizione,larghezza, lunghezza,prezzo, quantita, tipoMaterialeMaterasso,coloreLetto, materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino FROM Prodotto WHERE quantita=0");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prodotto p = new Prodotto();
                p.setIdProdotto(rs.getString(1));
                p.setNomeCategoria(rs.getString(2));
                p.setNomeProd(rs.getString(3));
                p.setDescrizione(rs.getString(4));
                p.setLarghezza(rs.getDouble(5));
                p.setLunghezza(rs.getDouble(6));
                p.setPrezzo(rs.getDouble(7));
                p.setQuantita(rs.getInt(8));
                p.setTipoMaterialeMaterasso(rs.getString(9));
                p.setColoreLetto(rs.getString(10));
                p.setMaterialeRete(rs.getString(11));
                p.setRivestimentoDivano(rs.getString(12));
                p.setColoreDivano(rs.getString(13));
                p.setTipoStoffaCuscino(rs.getString(14));
                p.setMaterialeCuscino(rs.getString(15));
                p.setFormaCuscino(rs.getString(16));
                prodottiEsauriti.add(p);
            }
            return prodottiEsauriti;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveQuantitaEsaurita", e);
        }
    }

    public static void cancellaProdotto(String idProdotto) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM Prodotto WHERE idProdotto=?",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, idProdotto);
            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("DELETE error.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in cancellaProdotto", e);
        }
    }


    public static void aggiuntaProdotto(Prodotto p) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Prodotto (idProdotto,nomeCategoria,nomeProd,descrizione,larghezza,lunghezza,prezzo,quantita,tipoMaterialeMaterasso ,coloreLetto ,materialeRete,rivestimentoDivano,coloreDivano,tipoStoffaCuscino,materialeCuscino,formaCuscino) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getIdProdotto());
            ps.setString(2, p.getNomeCategoria());
            ps.setString(3, p.getNomeProd());
            ps.setString(4, p.getDescrizione());
            ps.setDouble(5, p.getLarghezza());
            ps.setDouble(6, p.getLunghezza());
            ps.setDouble(7, p.getPrezzo());
            ps.setInt(8, p.getQuantita());
            ps.setString(9, p.getTipologiaMaterasso());
            ps.setString(10, p.getColoreLetto());
            ps.setString(11, p.getMaterialeRete());
            ps.setString(12, p.getRivestimentoDivano());
            ps.setString(13, p.getColoreDivano());
            ps.setString(14, p.getTipoStoffaCuscino());
            ps.setString(15, p.getMaterialeCuscino());
            ps.setString(16, p.getFormaCuscino());

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Errore nel definire il prodotto");

        } catch (SQLException e) {
            throw new DataAccessException("Errore in aggiuntaProdotto", e);
        }
    }
}