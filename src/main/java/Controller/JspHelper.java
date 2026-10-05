package Controller;

import Model.Prodotto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility per la logica comune nei JSP.
 * Centralizza l'estrazione dei dati dalla request, evitando scriptlet
 * con cast non controllati sparsi nei file JSP.
 */
public final class JspHelper {

    private static final String DEFAULT_FILTER_ATTRIBUTE = "filtra";
    private static final String FILTRAGGIO_ATTRIBUTE = "filtraggio";

    private JspHelper() {
        // Classe di utilita', non istanziabile
    }

    /**
     * Estrae la lista dei prodotti dalla request in base al parametro 'action'.
     *
     * @param request la request HTTP
     * @return la lista dei prodotti (mai null, restituisce lista vuota se assente)
     */
    @SuppressWarnings("unchecked")
    public static List<Prodotto> estraiProdotti(HttpServletRequest request) {
        String action = request.getParameter("action");
        String attributeKey = (action != null) ? action : DEFAULT_FILTER_ATTRIBUTE;
        Object attr = request.getAttribute(attributeKey);
        if (attr == null) {
            return new ArrayList<>();
        }
        return (List<Prodotto>) attr;
    }

    /**
     * Estrae il titolo della pagina dalla request.
     *
     * @param request la request HTTP
     * @return il titolo (mai null, restituisce stringa vuota se assente)
     */
    public static String estraiTitolo(HttpServletRequest request) {
        String action = request.getParameter("action");
        if (action != null) {
            return action;
        }
        Object filtraggio = request.getAttribute(FILTRAGGIO_ATTRIBUTE);
        return (filtraggio == null) ? "" : filtraggio.toString();
    }
}