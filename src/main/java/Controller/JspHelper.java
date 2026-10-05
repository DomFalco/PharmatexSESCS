package Controller;

import Model.Prodotto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;

/**
 * Utility per la logica comune nei JSP.
 * Centralizza l'estrazione dei dati dalla request, evitando scriptlet
 * con cast non controllati sparsi nei file JSP.
 */
public final class JspHelper {

    private JspHelper() {
        // Classe di utilita', non istanziabile
    }

    /**
     * Estrae la lista dei prodotti dalla request in base al parametro 'action'.
     * Se 'action' e' presente, usa request.getAttribute(action).
     * Altrimenti usa request.getAttribute("filtra").
     *
     * @param request la request HTTP
     * @return la lista dei prodotti (mai null, restituisce lista vuota se assente)
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Prodotto> estraiProdotti(HttpServletRequest request) {
        String action = request.getParameter("action");
        Object attr = (action != null)
                ? request.getAttribute(action)
                : request.getAttribute("filtra");
        if (attr == null) {
            return new ArrayList<>();
        }
        return (ArrayList<Prodotto>) attr;
    }

    /**
     * Estrae il titolo della pagina dalla request.
     * Se 'action' e' presente, restituisce il valore di 'action'.
     * Altrimenti restituisce l'attributo 'filtraggio'.
     *
     * @param request la request HTTP
     * @return il titolo (mai null, restituisce stringa vuota se assente)
     */
    public static String estraiTitolo(HttpServletRequest request) {
        String action = request.getParameter("action");
        if (action != null) {
            return action;
        }
        Object filtraggio = request.getAttribute("filtraggio");
        return filtraggio != null ? filtraggio.toString() : "";
    }
}