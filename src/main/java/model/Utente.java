package model;

import java.io.Serializable;
import org.mindrot.jbcrypt.BCrypt;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataDiNascita;
    private String email;
    private String password;
    private String nome;
    private String cognome;
    private String numeroTelefono;
    private String codiceFiscale;
    private String via;
    private String citta;
    private String cap;
    private String provincia;
    private String nazione;
    private boolean amministratore;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public String getVia() {
        return via;
    }

    public String getCitta() {
        return citta;
    }

    public String getCap() {
        return cap;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getNazione() {
        return nazione;
    }

    public String getDataDiNascita() {
        return dataDiNascita;
    }

    public boolean isAmministratore() {
        return amministratore;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        if (password != null && password.length() == 60 && password.startsWith("$2")) {
            // È già un hash BCrypt (recuperato dal DB), non ri-hashare
            this.password = password;
        } else {
            // È una password in chiaro (es. durante la registrazione), applica l'hash
            this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
        }
    }

    public boolean checkPassword(String plainPassword) {
        if (this.password == null || plainPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, this.password);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public void setDataDiNascita(String dataDiNascita) {
        this.dataDiNascita = dataDiNascita;
    }

    public void setAmministratore(boolean amministratore) {
        this.amministratore = amministratore;
    }
}