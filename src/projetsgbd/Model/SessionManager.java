package projetsgbd.Model;
import java.sql.Connection;

/**
 * Gestionnaire de session utilisateur
 */
public class SessionManager {
    
    // Variables statiques
    private static Connection connection;
    private static String currentUserLogin;
private static String currentPoste;  

public static void setCurrentPoste(String codePoste) {
    currentPoste = codePoste;
}

public static String getCurrentPoste() {
    return currentPoste;
}

    private SessionManager() {} // Constructeur privé

    // Setters
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    public static void setCurrentUser(String login) {
        currentUserLogin = login;
    }

    // Getters
    public static Connection getConnection() {
        return connection;
    }

    public static String getCurrentUser() {
        return currentUserLogin;
    }
}
