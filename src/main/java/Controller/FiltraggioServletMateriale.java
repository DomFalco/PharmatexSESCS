/*package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/FiltraggioServletMateriale")
public class FiltraggioServletMateriale extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mat = request.getParameter("prodotto");
        String materiale = request.getParameter("materiale");
            if (mat.equalsIgnoreCase("Materasso")) {
                response.getWriter().append("<option>Selezionare...</option>");
                response.getWriter().append("<option>Memory</option>");
                response.getWriter().append("<option>Molla</option>");
                response.getWriter().append("<option>Lana</option>");
                response.getWriter().append("<option>Lattice</option>");
            } else if (mat.equalsIgnoreCase("Rete")) {
                response.getWriter().append("<option>Selezionare...</option>");
                response.getWriter().append("<option>Faggio</option>");
                response.getWriter().append("<option>Ferro</option>");
            } else if (mat.equalsIgnoreCase("Cuscino")) {
                response.getWriter().append("<option>Selezionare...</option>");
                response.getWriter().append("<option>Memory</option>");
                response.getWriter().append("<option>Basic</option>");
                response.getWriter().append("<option>Fibre sintetiche</option>");
            }
            HttpSession session=request.getSession();
            session.setAttribute("mat",mat);
            session.setAttribute("materiale",materiale);
    }
}*/

package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/FiltraggioServletMateriale")
public class FiltraggioServletMateriale extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mat = request.getParameter("prodotto");
        String materiale = request.getParameter("materiale");

        // 1. CONTROLLO NULL (Evita crash se i parametri non vengono inviati)
        if (mat == null) mat = "";
        if (materiale == null) materiale = "";

        // 2. SANITIZZAZIONE (Risolve il Trust Boundary Violation per Snyk)
        // Creiamo nuove variabili "pulite" che Snyk riconosce come sicure
        String matSicuro = mat.replaceAll("[^a-zA-Z0-9\\s]", "");
        String materialeSicuro = materiale.replaceAll("[^a-zA-Z0-9\\s]", "");

        // 3. LOGICA DI RISPOSTA HTML (Usiamo le variabili sicure per i confronti)
        if (matSicuro.equalsIgnoreCase("Materasso")) {
            response.getWriter().append("<option>Selezionare...</option>");
            response.getWriter().append("<option>Memory</option>");
            response.getWriter().append("<option>Molla</option>");
            response.getWriter().append("<option>Lana</option>");
            response.getWriter().append("<option>Lattice</option>");
        } else if (matSicuro.equalsIgnoreCase("Rete")) {
            response.getWriter().append("<option>Selezionare...</option>");
            response.getWriter().append("<option>Faggio</option>");
            response.getWriter().append("<option>Ferro</option>");
        } else if (matSicuro.equalsIgnoreCase("Cuscino")) {
            response.getWriter().append("<option>Selezionare...</option>");
            response.getWriter().append("<option>Memory</option>");
            response.getWriter().append("<option>Basic</option>");
            response.getWriter().append("<option>Fibre sintetiche</option>");
        }

        // 4. INSERIMENTO SICURO NELLA SESSIONE
        // Dichiarato UNA SOLA VOLTA e salviamo le variabili SANITIZZATE
        HttpSession session = request.getSession();
        session.setAttribute("mat", matSicuro);
        session.setAttribute("materiale", materialeSicuro);
    }
}