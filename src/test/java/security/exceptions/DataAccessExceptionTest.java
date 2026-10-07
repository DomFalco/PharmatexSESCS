package security.exceptions;

import Model.DataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test minimale di copertura per DataAccessException.
 * Verifica i 2 costruttori per garantire coverage SonarQube.
 */
class DataAccessExceptionTest {

    @Test
    @DisplayName("Costruttore con solo message imposta correttamente il messaggio")
    void testCostruttoreConMessage() {
        DataAccessException ex = new DataAccessException("errore di test");
        assertThat(ex.getMessage()).isEqualTo("errore di test");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("Costruttore con message e cause imposta entrambi correttamente")
    void testCostruttoreConMessageECause() {
        Throwable cause = new RuntimeException("causa originale");
        DataAccessException ex = new DataAccessException("errore di test", cause);

        assertThat(ex.getMessage()).isEqualTo("errore di test");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}