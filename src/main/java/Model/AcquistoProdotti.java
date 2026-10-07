package Model;

import java.io.Serializable;

public class AcquistoProdotti implements Serializable {
    private static final long serialVersionUID = 1L;

    private int quantitaAcquistata;
    private Utente u;
    private Prodotto p;

    public AcquistoProdotti(Utente ut, Prodotto pr) {
        u = ut;
        p = pr;
    }

    public String getIdProdotto() {
        return p.getIdProdotto();
    }

    public void setIdProdotto(String idProdotto) {
        p.setIdProdotto(idProdotto);
    }

    public String getNomeProd() {
        return p.getNomeProd();
    }

    public void setNomeProd(String nomeProd) {
        p.setNomeProd(nomeProd);
    }

    public String getNome() {
        return u.getNome();
    }

    public void setNome(String nome) {
        u.setNome(nome);
    }

    public String getCognome() {
        return u.getCognome();
    }

    public void setCognome(String cognome) {
        u.setCognome(cognome);
    }

    public String getVia() {
        return u.getVia();
    }

    public void setVia(String via) {
        u.setVia(via);
    }

    public String getCitta() {
        return u.getCitta();
    }

    public void setCitta(String citta) {
        u.setCitta(citta);
    }

    public String getCap() {
        return u.getCap();
    }

    public void setCap(String cap) {
        u.setCap(cap);
    }

    public String getProvincia() {
        return u.getProvincia();
    }

    public void setProvincia(String provincia) {
        u.setProvincia(provincia);
    }

    public Double getPrezzo() {
        return p.getPrezzo();
    }

    public void setPrezzo(Double prezzo) {
        p.setPrezzo(prezzo);
    }

    public int getQuantitaAcquistata() {
        return quantitaAcquistata;
    }

    public void setQuantitaAcquistata(int quantitaAcquistata) {
        this.quantitaAcquistata = quantitaAcquistata;
    }
}