package model;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String numeroCarta;
    private String nomeIntestatario;
    private String dataScadenza;
    private String cvv;

    private final Utente u;

    public Carta(Utente ut) {
        u = ut;
    }

    public String getNumeroCarta() {
        return numeroCarta;
    }

    public void setNumeroCarta(String numeroCarta) {
        this.numeroCarta = numeroCarta;
    }

    public String getNomeIntestatario() {
        return nomeIntestatario;
    }

    public void setNomeIntestatario(String nomeIntestatario) {
        this.nomeIntestatario = nomeIntestatario;
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

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}