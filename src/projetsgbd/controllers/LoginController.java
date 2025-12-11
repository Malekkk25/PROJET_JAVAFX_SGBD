package projetsgbd.controllers;


import java.sql.SQLException;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import projetsgbd.ConnexionBD;
import projetsgbd.DAO.UtilisateurDAO;
import projetsgbd.Model.SessionManager;

public class LoginController {
    
    @FXML private VBox root;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtMdp;
    @FXML private Button btnConnexion;
    @FXML private Label lblMessage;
    private Connection connection;
    private UtilisateurDAO utilisateurDAO;
    
    @FXML
    private void initialize() {
        try {
          
            initAnimations();
        } catch (Exception e) {
            lblMessage.setText("❌ Erreur BD");
            lblMessage.getStyleClass().add("error-message");
            System.out.println("ERREUR INIT: " + e.getMessage());
        }
        txtLogin.requestFocus();
    }
    
    private void initAnimations() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    @FXML
    private void handleConnexion() {
        String login = txtLogin.getText().trim();
        String mdp = txtMdp.getText();
        
        System.out.println("🎯 handleConnexion - login='" + login + "' mdp='" + mdp + "'");
        
        resetForm();
        
        if (login.isEmpty() || mdp.isEmpty()) {
            showMessage("⚠️ Login et mot de passe requis !", "error-message");
            shakeAnimation(txtLogin);
            return;
        }
        
        btnConnexion.setDisable(true);
        showMessage("🔍 Connexion en cours...", "loading-message");
        
        // IMMÉDIAT sans Timeline pour debug
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0.3), e -> verifierConnexion(login, mdp))
        );
        timeline.play();
    }
    
   private void verifierConnexion(String login, String mdp) {
    System.out.println("🔍 verifierConnexion (Oracle Direct) APPELÉE");

    try {
Connection conn = ConnexionBD.getConnection(login, mdp);
SessionManager.setConnection(conn);
SessionManager.setCurrentUser(login);
utilisateurDAO = new UtilisateurDAO(conn);

// récupérer le code poste
String codePoste = utilisateurDAO.getCodePosteByLogin(login);
SessionManager.setCurrentPoste(codePoste);
System.out.println("Code poste = " + codePoste);

System.out.println("✅ CONNEXION ORACLE RÉUSSIE : " + login);

showMessage("✅ CONNEXION OK ! Redirection...", "success-message");
successAnimation();
redirigerSelonRole();
    } catch (SQLException e) {
     
        System.out.println("💥 ERREUR ORACLE: " + e.getErrorCode() + " - " + e.getMessage());
        
        if (e.getErrorCode() == 1017) { 
            showMessage("❌ Login ou mot de passe incorrect", "error-message");
        } else {
            showMessage("❌ Erreur connexion BD : " + e.getMessage(), "error-message");
        }
        
   
        shakeAnimation(txtLogin);
        shakeAnimation(txtMdp);
        
    } catch (Exception e) {
       
        e.printStackTrace();
        showMessage("❌ Erreur inattendue : " + e.getMessage(), "error-message");
        
    } finally {
       
        btnConnexion.setDisable(false);
    }
}
private void redirigerSelonRole() {
    String codePoste = SessionManager.getCurrentPoste();
    if ("2".equals(codePoste)) {
        redirigerGestionCommandes();
    } else if ("3".equals(codePoste)) {
        redirigerGestionLivraisons();
    } else {
        // par défaut : admin ou autres → gestion utilisateurs
        redirigerGestionUtilisateurs();
    }
}

private void redirigerGestionCommandes() {
    try {
        URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionCommandes.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        GestionCommandesController controller = loader.getController();
        // créer CommandeDAO en utilisant la même connexion
        projetsgbd.DAO.CommandeDAO cmdDao = new projetsgbd.DAO.CommandeDAO(SessionManager.getConnection());
        controller.initializeData(cmdDao);

        Scene scene = new Scene(root, 1100, 600);
        Stage stage = (Stage) btnConnexion.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Gestion Commandes - " + SessionManager.getCurrentUser());
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        showMessage("❌ Erreur redirection Commandes : " + e.getMessage(), "error-message");
    }
}

private void redirigerGestionLivraisons() {
    try {
        URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionLivraisons.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        GestionLivraisonsController controller = loader.getController();
        projetsgbd.DAO.LivraisonDAO livDao = new projetsgbd.DAO.LivraisonDAO(SessionManager.getConnection());
        controller.initializeData(livDao);

        Scene scene = new Scene(root, 1100, 600);
        Stage stage = (Stage) btnConnexion.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Gestion Livraisons - " + SessionManager.getCurrentUser());
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        showMessage("❌ Erreur redirection Livraisons : " + e.getMessage(), "error-message");
    }
}

         private void redirigerGestionUtilisateurs() {
        try {
            System.out.println("🔄 Chargement de GestionUtilisateur.fxml...");
            
            // 1. Recherche du fichier FXML (avec gestion des variantes de chemin)
            URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionUtilisateur.fxml");
            if (fxmlUrl == null) {
                 fxmlUrl = getClass().getResource("/GestionUtilisateur.fxml");
            }

            // Sécurité si fichier introuvable
            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR CRITIQUE : Fichier FXML introuvable !");
                showMessage("❌ Erreur : Fichier vue introuvable", "error-message");
                btnConnexion.setDisable(false);
                return;
            }

            // 2. Chargement de la vue
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent newRoot = loader.load();
            
            // 3. PASSAGE DU DAO AU CONTROLLER SUIVANT (Etape CRUCIALE)
            GestionUtilisateursController controller = loader.getController();
            
            if (this.utilisateurDAO != null) {
                // On injecte le DAO connecté
                controller.initializeData(this.utilisateurDAO);
                System.out.println("✅ DAO transmis avec succès au contrôleur suivant !");
            } else {
                // Cas impossible si verifierConnexion a réussi, mais on protège quand même
                System.err.println("❌ ERREUR : Le DAO est null au moment de la redirection !");
                showMessage("❌ Erreur interne : DAO non initialisé", "error-message");
                return; 
            }
            
            // 4. Affichage de la nouvelle scène
            Scene scene = new Scene(newRoot, 1100, 600);
            Stage stage = (Stage) btnConnexion.getScene().getWindow();
            stage.setScene(scene);
            
            // Titre personnalisé avec le nom de l'utilisateur connecté
            stage.setTitle("Gestion Utilisateurs - Connecté : " + SessionManager.getCurrentUser());
            
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("✅ REDIRECTION TERMINÉE AVEC SUCCÈS !");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR LORS DE LA REDIRECTION :");
            e.printStackTrace();
            showMessage("❌ Erreur redirection : " + e.getMessage(), "error-message");
            btnConnexion.setDisable(false);
        }
    }




    private void successAnimation() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), root);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    private void shakeAnimation(Control control) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), control);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    private void showMessage(String message, String styleClass) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().clear();
        lblMessage.getStyleClass().add(styleClass);
    }
    
    private void resetForm() {
        lblMessage.setText("");
        lblMessage.getStyleClass().clear();
        txtLogin.getStyleClass().remove("field-input-error");
        txtMdp.getStyleClass().remove("field-input-error");
    }
    
    @FXML
    private void handleAnnuler() {
        txtLogin.clear();
        txtMdp.clear();
        resetForm();
        txtLogin.requestFocus();
    }
}