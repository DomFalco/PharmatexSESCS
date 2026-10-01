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

        // 1. VALIDAZIONE INPUT (Risolve il Trust Boundary Violation)
        if (mat == null || (!mat.equalsIgnoreCase("Materasso") && !mat.equalsIgnoreCase("Rete") && !mat.equalsIgnoreCase("Cuscino"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'prodotto' non valido o mancante.");
            return;
        }

        // 2. VALIDAZIONE 'materiale'
        if (materiale == null || materiale.trim().isEmpty()) {
            materiale = "Selezionare...";
        }

        // 3. LOGICA DI RISPOSTA HTML
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

        // 4. INSERIMENTO SICURO NELLA SESSIONE
        HttpSession session = request.getSession();
        session.setAttribute("mat", mat);
        session.setAttribute("materiale", materiale);
    }
}