package Controller;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility per inviare risposte di errore HTTP in modo sicuro.
 * response.sendError() dichiara throws IOException. Questa eccezione si verifica
 * solo se il client si disconnette prima che la risposta sia completata: in quel
 * caso non c'e' nulla di utile da fare lato server. Catturarla qui evita di
 * propagarla al container e di generare errori nei log.
 */
public final class ServletErrorHelper {

    private static final Logger LOGGER = Logger.getLogger(ServletErrorHelper.class.getName());

    private ServletErrorHelper() {
        // Classe di utilita', non istanziabile
    }

    /**
     * Invia una risposta di errore HTTP gestendo l'eventuale IOException.
     *
     * @param response la risposta HTTP
     * @param status   il codice di stato (es. SC_FORBIDDEN, SC_BAD_REQUEST)
     * @param message  il messaggio di errore
     */
    public static void sendError(HttpServletResponse response, int status, String message) {
        try {
            response.sendError(status, message);
        } catch (IOException e) {
            // Il client si e' disconnesso prima che la risposta fosse inviata.
            LOGGER.log(Level.FINE, "Impossibile inviare la risposta di errore {0}: {1}",
                    new Object[]{status, e.getMessage()});
        }
    }
}