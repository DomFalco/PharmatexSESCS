package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/FiltraggioServletMateriale")
public class FiltraggioServletMateriale extends HttpServlet {

    // ===== Costanti =====
    private static final String OPTION_SELEZIONARE = "<option>Selezionare...</option>";

    private static final String CATEGORIA_MATERASSO = "Materasso";
    private static final String CATEGORIA_RETE = "Rete";
    private static final String CATEGORIA_CUSCINO = "Cuscino";

    private static final String ATTR_MAT = "mat";
    private static final String ATTR_MATERIALE = "materiale";

    @Override
    @SuppressWarnings("java:S1989")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mat = request.getParameter("prodotto");
        String materiale = request.getParameter("materiale");

        // 1. CONTROLLO NULL (evita crash se i parametri non vengono inviati)
        if (mat == null) mat = "";
        if (materiale == null) materiale = "";

        // 2. SANITIZZAZIONE (risolve il Trust Boundary Violation per Snyk)
        String matSicuro = mat.replaceAll("[^a-zA-Z0-9\\s]", "");
        String materialeSicuro = materiale.replaceAll("[^a-zA-Z0-9\\s]", "");

        // 3. LOGICA DI RISPOSTA HTML (usa le variabili sicure per i confronti)
        PrintWriter out = response.getWriter();

        if (matSicuro.equalsIgnoreCase(CATEGORIA_MATERASSO)) {
            scriviOption(out, OPTION_SELEZIONARE,
                    "<option>Memory</option>",
                    "<option>Molla</option>",
                    "<option>Lana</option>",
                    "<option>Lattice</option>");
        } else if (matSicuro.equalsIgnoreCase(CATEGORIA_RETE)) {
            scriviOption(out, OPTION_SELEZIONARE,
                    "<option>Faggio</option>",
                    "<option>Ferro</option>");
        } else if (matSicuro.equalsIgnoreCase(CATEGORIA_CUSCINO)) {
            scriviOption(out, OPTION_SELEZIONARE,
                    "<option>Memory</option>",
                    "<option>Basic</option>",
                    "<option>Fibre sintetiche</option>");
        }

        // 4. INSERIMENTO SICURO NELLA SESSIONE
        HttpSession session = request.getSession();
        session.setAttribute(ATTR_MAT, matSicuro);
        session.setAttribute(ATTR_MATERIALE, materialeSicuro);
    }

    /**
     * Scrive una lista di option HTML sul writer di risposta.
     * Il primo elemento è tipicamente il placeholder "Selezionare...".
     */
    private void scriviOption(PrintWriter out, String... option) {
        for (String o : option) {
            out.append(o);
        }
    }
}