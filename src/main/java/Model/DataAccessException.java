package Model;

/**
 * Eccezione custom per gli errori di accesso al database.
 * Estende RuntimeException per non forzare checked exceptions sui metodi dei DAO,
 * ma ha un nome specifico che ne identifica la causa.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}