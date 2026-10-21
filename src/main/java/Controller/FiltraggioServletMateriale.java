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
        try {
            String mat = request.getParameter("prodotto");
            String materiale = request.getParameter(ATTR_MATERIALE);

            if (mat == null) mat = "";
            if (materiale == null) materiale = "";

            String matSicuro = mat.replaceAll("[^a-zA-Z0-9\\s]", "");
            String materialeSicuro = materiale.replaceAll("[^a-zA-Z0-9\\s]", "");

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

            HttpSession session = request.getSession();
            session.setAttribute(ATTR_MAT, matSicuro);
            session.setAttribute(ATTR_MATERIALE, materialeSicuro);

        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la generazione della risposta.");
        }
    }

    private void scriviOption(PrintWriter out, String... option) {
        for (String o : option) {
            out.append(o);
        }
    }
}