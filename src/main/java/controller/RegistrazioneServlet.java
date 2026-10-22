package controller;

import model.Utente;
import model.UtenteDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/RegistrazioneServlet")
public class RegistrazioneServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegistrazioneServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Utente u = new Utente();
            u.setEmail(request.getParameter("email"));
            u.setPassword(request.getParameter("passwordEmail"));
            u.setNome(request.getParameter("nome"));
            u.setCognome(request.getParameter("cognome"));
            u.setDataDiNascita(request.getParameter("datadiNascita"));
            u.setNumeroTelefono(request.getParameter("numeroTelefono"));
            u.setCodiceFiscale(request.getParameter("codiceFiscale"));
            u.setVia(request.getParameter("via"));
            u.setCitta(request.getParameter("citta"));
            u.setCap(request.getParameter("cap"));
            u.setProvincia(request.getParameter("provincia"));
            u.setNazione(request.getParameter("nazione"));
            u.setAmministratore(false);

            if(UtenteDAO.controlloEmail(request.getParameter("email"))) {
                request.setAttribute("controllo","Email già presente");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/RegisterUser.jsp");
                dispatcher.forward(request, response);
            } else {
                UtenteDAO.doRegistrazione(u);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Login.jsp");
                dispatcher.forward(request, response);
            }
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la registrazione dell'utente.");
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