package controller;

import model.Prodotto;
import model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/AggiuntaProdottoServlet")
public class AggiuntaProdottoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AggiuntaProdottoServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Prodotto p = new Prodotto();
            p.setIdProdotto(request.getParameter("idProdotto"));
            p.setNomeCategoria(request.getParameter("nomeCategoria"));
            p.setNomeProd(request.getParameter("nomeProdotto"));
            p.setDescrizione(request.getParameter("descrizione"));
            p.setLarghezza(Double.parseDouble(request.getParameter("larghezza")));
            p.setLunghezza(Double.parseDouble(request.getParameter("lunghezza")));
            p.setPrezzo(Double.parseDouble(request.getParameter("prezzo")));
            p.setQuantita(Integer.parseInt(request.getParameter("quantita")));
            p.setTipoMaterialeMaterasso(request.getParameter("tipoMaterialeMaterasso"));
            p.setColoreLetto(request.getParameter("coloreLetto"));
            p.setMaterialeRete(request.getParameter("materialeRete"));
            p.setRivestimentoDivano(request.getParameter("rivestimentoDivano"));
            p.setColoreDivano(request.getParameter("coloreDivano"));
            p.setTipoStoffaCuscino(request.getParameter("tipoStoffaCuscino"));
            p.setMaterialeCuscino(request.getParameter("materialeCuscino"));
            p.setFormaCuscino(request.getParameter("formaCuscino"));

            ProdottoDAO.aggiuntaProdotto(p);

            RequestDispatcher dispatcher = request.getRequestDispatcher("HomeServletAmministratore");
            dispatcher.forward(request, response);

        } catch (NumberFormatException | NullPointerException e) {
            inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Formato numerico non valido o parametro mancante.");
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'inoltro della richiesta.");
        }
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}