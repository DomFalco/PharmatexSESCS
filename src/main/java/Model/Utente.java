package Model;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataDiNascita, email, password, nome, cognome, numeroTelefono, codiceFiscale, via, citta, cap, provincia, nazione;
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            this.password = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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