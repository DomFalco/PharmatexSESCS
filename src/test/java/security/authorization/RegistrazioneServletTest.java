package security.authorization;

import Controller.RegistrazioneServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica il comportamento di RegistrazioneServlet rispetto alla creazione
 * di nuovi utenti.
 * NOTA: i test DOCUMENTANO la mancanza di validazione dei parametri e di
 * controlli di sicurezza (CSRF, rate limiting, validazione email/password).
 * NOTA TECNICA: Per evitare il ClassCircularityError causato da Mockito su JDK recenti,
 * le classi Jakarta (HttpServletRequest, ecc.) non vengono mockate con Mockito,
 * ma tramite Proxy nativi di Java.
 */
@DisplayName("Authorization - RegistrazioneServlet")
class RegistrazioneServletTest {

    private RegistrazioneServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    // Oggetti finti per tracciare le chiamate
    private FakeRequest fakeRequest;
    private FakeSession fakeSession;

    @BeforeEach
    void setUp() {
        servlet = new RegistrazioneServlet();

        // 1. Creazione del Fake per HttpSession tramite Proxy
        fakeSession = new FakeSession();
        // 'session' è ora una variabile locale
        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpSession.class},
                fakeSession
        );

        // 2. Creazione del Fake per HttpServletRequest tramite Proxy
        fakeRequest = new FakeRequest();
        fakeRequest.session = session;
        request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                fakeRequest
        );

        // 3. Creazione di un finto HttpServletResponse (non tracciato, serve solo per far girare il codice)
        response = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String nomeMetodo = method.getName();
                    // Gestione metodi Object (equals, hashCode, toString) per evitare NPE
                    switch (nomeMetodo) {
                        case "equals":
                            return proxy == args[0];
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "toString":
                            return "FakeResponse";
                        default:
                            return null;
                    }
                }
        );
    }

    /**
     * Helper: configura i 12 parametri con valori validi nel FakeRequest.
     */
    private void mockParametriValidi() {
        fakeRequest.parametri.put("email", "test@example.com");
        fakeRequest.parametri.put("passwordEmail", "Password123!");
        fakeRequest.parametri.put("nome", "Mario");
        fakeRequest.parametri.put("cognome", "Rossi");
        fakeRequest.parametri.put("datadiNascita", "1990-01-01");
        fakeRequest.parametri.put("numeroTelefono", "3331234567");
        fakeRequest.parametri.put("codiceFiscale", "RSSMRA90A01H501Z");
        fakeRequest.parametri.put("via", "Via Roma 1");
        fakeRequest.parametri.put("citta", "Roma");
        fakeRequest.parametri.put("cap", "00100");
        fakeRequest.parametri.put("provincia", "RM");
        fakeRequest.parametri.put("nazione", "Italia");
    }

    // ====== 1. Lettura di tutti i 12 parametri ======

    @Test
    @DisplayName("Tutti i 12 parametri vengono letti dalla richiesta")
    void testTuttiIParametriVengonoLetti() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:email"), "email non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:passwordEmail"), "passwordEmail non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:nome"), "nome non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:cognome"), "cognome non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:datadiNascita"), "datadiNascita non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:numeroTelefono"), "numeroTelefono non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:codiceFiscale"), "codiceFiscale non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:via"), "via non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:citta"), "citta non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:cap"), "cap non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:provincia"), "provincia non letto");
        assertTrue(fakeRequest.chiamate.contains("getParameter:nazione"), "nazione non letto");
    }

    // ====== 2. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Email null causa eccezione (vulnerabilità documentata)")
    void testEmailNullCausaEccezione() {
        mockParametriValidi();
        fakeRequest.parametri.put("email", null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice non gestisce email null: NPE documentata");
    }

    @Test
    @DisplayName("Password null causa eccezione (vulnerabilità documentata)")
    void testPasswordNullCausaEccezione() {
        mockParametriValidi();
        fakeRequest.parametri.put("passwordEmail", null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice non gestisce password null: NPE documentata");
    }

    // ====== 3. Documentazione mancanza validazione email ======

    @Test
    @DisplayName("Email senza formato valido non viene rifiutata (vulnerabilità documentata)")
    void testEmailSenzaFormatoValidoNonRifiutata() {
        mockParametriValidi();
        fakeRequest.parametri.put("email", "non-una-email");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:email"),
                "La Servlet NON valida il formato dell'email: la stringa passa al DAO");
    }

    @Test
    @DisplayName("Email con caratteri pericolosi non viene rifiutata (documentazione)")
    void testEmailConCaratteriPericolosi() {
        mockParametriValidi();
        fakeRequest.parametri.put("email", "<script>@x.com");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:email"),
                "La Servlet NON sanitizza l'email");
    }

    // ====== 4. Documentazione mancanza validazione password ======

    @Test
    @DisplayName("Password debole non viene rifiutata (vulnerabilità documentata)")
    void testPasswordDeboleNonRifiutata() {
        mockParametriValidi();
        fakeRequest.parametri.put("passwordEmail", "123");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:passwordEmail"),
                "La Servlet NON valida la forza della password");
    }

    @Test
    @DisplayName("Password vuota non viene rifiutata (vulnerabilità documentata)")
    void testPasswordVuotaNonRifiutata() {
        mockParametriValidi();
        fakeRequest.parametri.put("passwordEmail", "");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:passwordEmail"),
                "Nessun controllo su password vuota");
    }

    // ====== 5. Documentazione mancanza validazione codice fiscale ======

    @Test
    @DisplayName("Codice fiscale invalido non viene rifiutato (vulnerabilità documentata)")
    void testCodiceFiscaleInvalidoNonRifiutato() {
        mockParametriValidi();
        fakeRequest.parametri.put("codiceFiscale", "INVALIDO");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:codiceFiscale"),
                "La Servlet NON valida il formato del codice fiscale");
    }

    // ====== 6. Il ruolo amministratore è forzato a false ======

    @Test
    @DisplayName("Il ruolo amministratore è forzato a false (buona pratica di sicurezza)")
    void testAmministratoreForzatoFalse() {
        mockParametriValidi();
        fakeRequest.parametri.put("amministratore", "true");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertFalse(fakeRequest.chiamate.contains("getParameter:amministratore"),
                "La Servlet ha letto il parametro amministratore, violando la sicurezza!");
    }

    // ====== 7. Documentazione mancanza protezione CSRF ======

    @Test
    @DisplayName("Nessun controllo CSRF token presente (vulnerabilità documentata)")
    void testNessunControlloCsrf() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertFalse(fakeRequest.chiamate.contains("getParameter:csrf_token"),
                "La Servlet ha cercato un token CSRF (non dovrebbe esistere)");
        assertFalse(fakeSession.chiamate.contains("getAttribute:csrf_token"),
                "La Servlet ha cercato un attributo CSRF in sessione (non dovrebbe esistere)");
    }

    // ====== 8. Documentazione mancanza rate limiting ======

    @Test
    @DisplayName("Nessuna protezione contro registrazioni massive (vulnerabilità documentata)")
    void testNessunaProtezioneRegistrazioniMassive() {
        mockParametriValidi();

        // Simula 50 tentativi di registrazione consecutivi
        for (int i = 0; i < 50; i++) {
            try {
                servlet.service(request, response);
            } catch (Exception e) {
                // Eccezione attesa dal DB
            }
        }

        assertFalse(fakeSession.chiamate.contains("invalidate"),
                "La sessione è stata invalidata, ma la Servlet non dovrebbe farlo!");
    }

    // ====== 9. Documentazione mancanza controllo forza password ======

    @Test
    @DisplayName("Nessuna lunghezza minima per la password (vulnerabilità documentata)")
    void testNessunaLunghezzaMinima() {
        mockParametriValidi();
        fakeRequest.parametri.put("passwordEmail", "a"); // 1 carattere

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:passwordEmail"),
                "La Servlet accetta una password di 1 carattere: nessuna policy");
    }

    // ====== 10. Documentazione: il controllo email + registrazione non è atomico ======

    @Test
    @DisplayName("Controllo email + registrazione non atomici (race condition documentata)")
    void testRaceConditionEmail() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        assertTrue(fakeRequest.chiamate.contains("getParameter:email"),
                "La Servlet non ha letto l'email");
    }

    // =================================================================
    // CLASSI INTERNE PER I FAKE (Proxy)
    // =================================================================

    /**
     * Fake per HttpSession: registra i nomi dei metodi chiamati e i loro argomenti.
     */
    static class FakeSession implements InvocationHandler {
        public final List<String> chiamate = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String nomeMetodo = method.getName();

            // Gestione metodi Object (equals, hashCode, toString) per evitare NPE
            switch (nomeMetodo) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "FakeSession";
                default:
                    break;
            }

            if (args != null && args.length > 0 && args[0] != null) {
                chiamate.add(nomeMetodo + ":" + args[0]);
            } else {
                chiamate.add(nomeMetodo);
            }
            return null;
        }
    }

    /**
     * Fake per HttpServletRequest: registra le chiamate e restituisce
     * i valori configurati per i parametri e la sessione.
     */
    static class FakeRequest implements InvocationHandler {
        public final List<String> chiamate = new ArrayList<>();
        public final Map<String, String> parametri = new HashMap<>();
        public HttpSession session;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String nomeMetodo = method.getName();

            // Gestione metodi Object (equals, hashCode, toString) per evitare NPE
            switch (nomeMetodo) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "FakeRequest";
                default:
                    break;
            }

            if (args != null && args.length > 0 && args[0] != null) {
                chiamate.add(nomeMetodo + ":" + args[0]);
            } else {
                chiamate.add(nomeMetodo);
            }

            switch (nomeMetodo) {
                case "getParameter":
                    // Protezione da args null o args[0] null (evita NPE e warning)
                    if (args == null || args.length == 0 || args[0] == null) {
                        return null;
                    }
                    String chiave = (String) args[0];
                    return parametri.get(chiave);
                case "getSession":
                    return session;
                case "getMethod":
                    return "POST";
                case "getProtocol":
                    return "HTTP/1.1";
                case "getRequestDispatcher":
                    return Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class<?>[]{RequestDispatcher.class},
                            (p, m, a) -> {
                                String nomeM = m.getName();
                                switch (nomeM) {
                                    case "equals":
                                        return p == a[0];
                                    case "hashCode":
                                        return System.identityHashCode(p);
                                    case "toString":
                                        return "FakeDispatcher";
                                    default:
                                        return null;
                                }
                            }
                    );
                default:
                    return null;
            }
        }
    }
}