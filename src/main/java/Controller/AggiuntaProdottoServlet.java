package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet("/AggiuntaProdottoServlet")
public class AggiuntaProdottoServlet extends HttpServlet {
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
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico non valido o parametro mancante.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'inoltro della richiesta.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}