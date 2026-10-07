package security.functional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Funzioni di supporto per i test H2.
 * Questa classe contiene metodi Java statici che vengono registrati come "alias"
 * di funzioni SQL in H2. Permette di usare in H2 le stesse funzioni
 * MySQL-specifiche usate dal codice di produzione (es. SHA1()), senza
 * modificare il codice di produzione.
 * Esempio di registrazione (vedi BaseH2Test.java):
 * <pre>
 *   CREATE ALIAS IF NOT EXISTS SHA1 FOR "security.functional.TestFunctions.sha1"
 * </pre>
 * Da quel momento, ogni query H2 che usa SHA1(?) chiama il metodo qui sotto.
 */
public class TestFunctions {

    /**
     * Implementazione Java di SHA-1, equivalente alla funzione SHA1() di MySQL.
     * Restituisce la stringa esadecimale (40 caratteri, minuscola).
     * @param input la stringa da hashare (puo' essere null)
     * @return l'hash SHA-1 in formato esadecimale, o null se input e' null
     */
    public static String sha1(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel calcolo SHA-1", e);
        }
    }
}