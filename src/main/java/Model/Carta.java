package Model;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;

    String numeroCarta, nomeIntestario, dataScadenza;
    String CVV;
    Utente u;

    public Carta(Utente ut) {
        u = ut;
    }

    public String getNumeroCarta() {
        return numeroCarta;
    }

    public void setNumeroCarta(String numeroCarta) {
        this.numeroCarta = numeroCarta;
    }

    public String getNomeIntestario() {
        return nomeIntestario;
    }

    public void setNomeIntestario(String nomeIntestario) {
        this.nomeIntestario = nomeIntestario;
    }

    public String getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(String dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public String getEmailProprietario() {
        return u.getEmail();
    }

    public void setEmailProprietario(String emailProprietario) {
        u.setEmail(emailProprietario);
    }

    public String getCVV() {
        return CVV;
    }

    public void setCVV(String CVV) {
        this.CVV = CVV;
    }
}