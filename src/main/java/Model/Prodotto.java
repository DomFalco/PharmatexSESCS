package Model;

import java.io.Serializable;

public class Prodotto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idProdotto, nomeProd, descrizione, tipoMaterialeMaterasso, tipoLetto, coloreLetto,
            materialeRete, rivestimentoDivano, coloreDivano, tipoStoffaCuscino, materialeCuscino, formaCuscino, nomeCategoria;
    private double larghezza, lunghezza, prezzo;
    private int quantita;

    public String getIdProdotto() {
        return idProdotto;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public String getNomeProd() {
        return nomeProd;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getTipologiaMaterasso() {
        return tipoMaterialeMaterasso;
    }

    public String getColoreLetto() {
        return coloreLetto;
    }

    public String getMaterialeRete() {
        return materialeRete;
    }

    public String getRivestimentoDivano() {
        return rivestimentoDivano;
    }

    public String getColoreDivano() {
        return coloreDivano;
    }

    public String getTipoStoffaCuscino() {
        return tipoStoffaCuscino;
    }

    public String getMaterialeCuscino() {
        return materialeCuscino;
    }

    public String getFormaCuscino() {
        return formaCuscino;
    }

    public double getLarghezza() {
        return larghezza;
    }

    public double getLunghezza() {
        return lunghezza;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setIdProdotto(String idProdotto) {
        this.idProdotto = idProdotto;
    }

    public void setNomeProd(String nomeProd) {
        this.nomeProd = nomeProd;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setTipoMaterialeMaterasso(String tipoMaterialeMaterasso) {
        this.tipoMaterialeMaterasso = tipoMaterialeMaterasso;
    }

    public void setColoreLetto(String coloreLetto) {
        this.coloreLetto = coloreLetto;
    }

    public void setMaterialeRete(String materialeRete) {
        this.materialeRete = materialeRete;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public void setRivestimentoDivano(String rivestimentoDivano) {
        this.rivestimentoDivano = rivestimentoDivano;
    }

    public void setColoreDivano(String coloreDivano) {
        this.coloreDivano = coloreDivano;
    }

    public void setTipoStoffaCuscino(String tipoStoffaCuscino) {
        this.tipoStoffaCuscino = tipoStoffaCuscino;
    }

    public void setMaterialeCuscino(String materialeCuscino) {
        this.materialeCuscino = materialeCuscino;
    }

    public void setFormaCuscino(String formaCuscino) {
        this.formaCuscino = formaCuscino;
    }

    public void setLarghezza(double larghezza) {
        this.larghezza = larghezza;
    }

    public void setLunghezza(double lunghezza) {
        this.lunghezza = lunghezza;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    @Override
    public String toString() {
        return "Prodotto{" +
                "idProdotto='" + idProdotto + '\'' +
                ", nomeProd='" + nomeProd + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", tipologiaMaterasso='" + tipoMaterialeMaterasso + '\'' +
                ", coloreLetto='" + coloreLetto + '\'' +
                ", materialeRete='" + materialeRete + '\'' +
                ", rivestimentoDivano='" + rivestimentoDivano + '\'' +
                ", coloreDivano='" + coloreDivano + '\'' +
                ", tipoStoffaCuscino='" + tipoStoffaCuscino + '\'' +
                ", materialeCuscino='" + materialeCuscino + '\'' +
                ", formaCuscino='" + formaCuscino + '\'' +
                ", nomeCategoria='" + nomeCategoria + '\'' +
                ", larghezza=" + larghezza +
                ", lunghezza=" + lunghezza +
                ", prezzo=" + prezzo +
                ", quantita=" + quantita +
                '}';
    }
}