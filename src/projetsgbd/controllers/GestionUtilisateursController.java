package projetsgbd.controllers;

import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import projetsgbd.DAO.UtilisateurDAO;
import projetsgbd.Model.Utilisateur;

public class GestionUtilisateursController {

    // ========== CHAMPS DU FORMULAIRE ==========
    @FXML private TextField loginField;
    @FXML private PasswordField motPasseField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    
    // ✅ MODIFICATION : ComboBox au lieu de TextField
    @FXML private ComboBox<String> posteCombo; 
    
    @FXML private TextField telField;
    @FXML private Button enregistrerButton;
@FXML private HBox tabUsers;
@FXML private HBox tabCommandes;
@FXML private HBox tabLivraisons;
@FXML private TextField searchField;

    // ========== TABLEAU ==========
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> colLogin;
    @FXML private TableColumn<Utilisateur, String> colNomPrenom;
    @FXML private TableColumn<Utilisateur, String> colPoste;
    @FXML private TableColumn<Utilisateur, String> colTel;
    @FXML private TableColumn<Utilisateur, String> colDateEmbauche;
    @FXML private TableColumn<Utilisateur, Void> colActions;

    // ========== DONNÉES ==========
    private final ObservableList<Utilisateur> data = FXCollections.observableArrayList();
    private UtilisateurDAO utilisateurDAO; 

        @FXML
    private void initialize() {
        // Configurer les colonnes
        colLogin.setCellValueFactory(cellData -> cellData.getValue().loginProperty());
        colNomPrenom.setCellValueFactory(cellData -> cellData.getValue().nomPrenomProperty());
        colPoste.setCellValueFactory(cellData -> cellData.getValue().posteProperty());
        colTel.setCellValueFactory(cellData -> cellData.getValue().telephoneProperty());
        colDateEmbauche.setCellValueFactory(cellData -> cellData.getValue().dateEmbaucheProperty());

        // ✅ CONFIGURATION DE LA COLONNE ACTIONS AVEC 3 BOUTONS
        colActions.setCellFactory(column -> new TableCell<Utilisateur, Void>() {
            private final Button btnEdit = new Button("✎ Éditer");
            private final Button btnGrant = new Button("🔑 Grant");
            private final Button btnDelete = new Button("🗑 Supprimer");

            {
                // Styles CSS pour les 3 boutons
                btnEdit.setStyle("-fx-padding: 5 10; -fx-font-size: 10px; -fx-background-color: #4c6fff; -fx-text-fill: white; -fx-cursor: hand;");
                btnGrant.setStyle("-fx-padding: 5 10; -fx-font-size: 10px; -fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-padding: 5 10; -fx-font-size: 10px; -fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-cursor: hand;");

                // Action Éditer
                btnEdit.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    chargerUtilisateurDansFormulaire(user);
                });

                // ✅ Action Grant (Nouveau)
                btnGrant.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    handleGrantUser(user);
                });

                // Action Supprimer
                btnDelete.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(user);
                });
            }

@Override
protected void updateItem(Void item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
        setGraphic(null);
    } else {
        Utilisateur user = getTableView().getItems().get(getIndex());

        // Si poste = 4 (livreur) → on cache le bouton Grant
        if ("4".equals(user.getPoste())) {
            btnGrant.setVisible(false);
            btnGrant.setManaged(false); // ne prend pas de place dans la HBox
        } else {
            btnGrant.setVisible(true);
            btnGrant.setManaged(true);
        }

        HBox box = new HBox(5, btnEdit, btnGrant, btnDelete);
        setGraphic(box);
    }
}

        });

        usersTable.setItems(data);
    }

    // ✅ MÉTHODE POUR GÉRER LE GRANT
      private void handleGrantUser(Utilisateur user) {
    if (utilisateurDAO == null) return;

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
        "Attribuer les droits à " + user.getLogin() + " ?",
        ButtonType.YES, ButtonType.NO);

    if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
        try {
            // Aller chercher le vrai mot de passe en base
            String mdp = utilisateurDAO.getMotDePasseByLogin(user.getLogin());
            if (mdp == null || mdp.isEmpty()) {
                afficherAlerte("Erreur",
                    "Mot de passe introuvable pour " + user.getLogin() +
                    " (colonne MOTP).", Alert.AlertType.ERROR);
                return;
            }

            String messageOracle = utilisateurDAO.grantUser(
                user.getLogin(),
                user.getPoste(),  // codePoste ("1","2","3")
                mdp               // VRAI mot de passe
            );

            afficherAlerte("Message Oracle", messageOracle, Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            // ton nettoyage d'erreur Oracle
            String rawMsg = e.getMessage();
            String finalMsg = rawMsg;
            if (rawMsg.contains(": ")) {
                finalMsg = rawMsg.substring(rawMsg.indexOf(": ") + 2);
            }
            if (finalMsg.contains("\n")) {
                finalMsg = finalMsg.substring(0, finalMsg.indexOf("\n"));
            }
            if (finalMsg.contains("ORA-06512")) {
                finalMsg = finalMsg.substring(0, finalMsg.indexOf("ORA-06512"));
            }
            afficherAlerte("Message Oracle", finalMsg.trim(), Alert.AlertType.ERROR);

        } catch (Exception e) {
            afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}



    /**
     * Injection du DAO et chargement initial
     */
    public void initializeData(UtilisateurDAO dao) {
        this.utilisateurDAO = dao;
        
        // =========================================================
        // ✅ AUTO-RÉPARATION : On tente de créer les vues au démarrage
        // (C'est sans danger si elles existent déjà avec CREATE OR REPLACE)
        try {
            utilisateurDAO.initialiserVues();
            System.out.println("✅ Vues Oracle initialisées avec succès.");
        } catch (SQLException e) {
            // On log juste l'erreur sans bloquer l'appli
            System.err.println("Info : Vues déjà existantes ou erreur mineure : " + e.getMessage());
        }
        // =========================================================

        // Charger les utilisateurs ET la liste des postes
 chargerUtilisateurs();   // remplit la TableView
chargerListePostes();    // remplit la ComboBox des postes

    }
    /**
     * Convertit le code poste (ex: "1", "2") en nom de rôle Oracle (ex: "ADMINISTRATEUR").
     * Cette méthode sert de table de correspondance.
     */

    private void chargerUtilisateurs() {
        if (utilisateurDAO == null) return;
        try {
            ObservableList<Utilisateur> utilisateurs = utilisateurDAO.chargerTousLesUtilisateurs();
            data.setAll(utilisateurs);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur chargement utilisateurs : " + e.getMessage());
        }
    }

    // ✅ NOUVELLE MÉTHODE : Charger la liste déroulante
    private void chargerListePostes() {
        if (utilisateurDAO == null) return;
        try {
            ObservableList<String> listePostes = utilisateurDAO.chargerListePostes();
            posteCombo.setItems(listePostes);
        } catch (Exception e) {
            System.err.println("Erreur chargement postes : " + e.getMessage());
        }
    }

        @FXML
    private void handleEnregistrer() {
        // 1. Récupération des champs texte
        String login = loginField.getText().trim();
        String mdp = motPasseField.getText().trim();
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String tel = telField.getText().trim();
        String adresse = adresseField.getText().trim();
        String ville = villeField.getText().trim();
        
        // 2. Récupération et traitement du POSTE (ComboBox)
        String selectionPoste = posteCombo.getValue(); // Ex: "2 - Magasinier"
        String codePoste = null;

        if (selectionPoste != null && !selectionPoste.isEmpty()) {
            // On découpe la chaîne pour ne garder que ce qui est avant le tiret
            // "2 - Magasinier" -> on garde "2"
            String[] parts = selectionPoste.split(" - ");
            if (parts.length > 0) {
                codePoste = parts[0].trim();
            }
        }

        // Validation locale minimale (le reste est fait par Oracle)
        if (codePoste == null || codePoste.isEmpty()) {
             afficherAlerte("Erreur", "Veuillez sélectionner un poste dans la liste.", Alert.AlertType.WARNING);
             return;
        }

        try {
            // 3. Création de l'objet Utilisateur avec le CODE du poste
            Utilisateur user = new Utilisateur(login, mdp, nom, prenom, tel, adresse, ville, codePoste);
            
            // 4. Appel au DAO (qui appelle la procédure stockée Oracle)
            boolean succes = utilisateurDAO.ajouterUtilisateur(user);

            // 5. Gestion du succès
            if (succes) {
                afficherAlerte("Succès", "Utilisateur ajouté avec succès !", Alert.AlertType.INFORMATION);
                chargerUtilisateurs(); // Rafraîchir le tableau
                clearForm();           // Vider le formulaire
            }
            
        } catch (SQLException e) {
            // 6. Gestion propre des erreurs Oracle (via ton package PKG_MESSAGES)
            String rawMessage = e.getMessage();
            String userMessage = rawMessage;

            // Nettoyage du message (suppression de ORA-XXXXX et de la pile d'erreur)
            int deuxPoints = rawMessage.indexOf(": ");
            if (deuxPoints != -1) {
                userMessage = rawMessage.substring(deuxPoints + 2);
            }
            int finMessage = userMessage.indexOf("\n");
            if (finMessage != -1) {
                userMessage = userMessage.substring(0, finMessage);
            }
            
            afficherAlerte("Erreur de validation", userMessage, Alert.AlertType.ERROR);
            
        } catch (Exception e) {
            // 7. Erreurs inattendues
            e.printStackTrace();
            afficherAlerte("Erreur système", "Une erreur inattendue est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


private void chargerUtilisateurDansFormulaire(Utilisateur user) {
    loginField.setText(user.getLogin());
    loginField.setEditable(false);

    try {
        String mdp = utilisateurDAO.getMotDePasseByLogin(user.getLogin());
        motPasseField.setText(mdp != null ? mdp : "");
    } catch (SQLException e) {
        motPasseField.setText("");
    }

    nomField.setText(user.getNom());
    prenomField.setText(user.getPrenom());
    telField.setText(user.getTelephone());
    adresseField.setText(user.getAdresse());
    villeField.setText(user.getVille());

        // ✅ SÉLECTION INTELLIGENTE DU POSTE
        // L'objet user contient juste le code (ex: "2").
        // La ComboBox contient "2 - Magasinier". Il faut trouver la bonne ligne.
        String codeUser = user.getPoste();
        
        if (codeUser != null) {
            for (String item : posteCombo.getItems()) {
                // On cherche l'item qui commence par "2 - " ou qui est égal à "2"
                if (item.startsWith(codeUser + " - ") || item.equals(codeUser)) {
                    posteCombo.setValue(item);
                    break;
                }
            }
        } else {
            posteCombo.setValue(null);
        }
        
        // Changement du bouton pour passer en mode "Modifier"
        enregistrerButton.setText("Modifier");
        enregistrerButton.setOnAction(event -> modifierUtilisateur(user));
    }

    
       private void modifierUtilisateur(Utilisateur userExistant) {
        // 1. Récupération des champs (sans vérification Java poussée)
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String tel = telField.getText().trim();
        String adresse = adresseField.getText().trim();
        String ville = villeField.getText().trim();
        
        // Récupération du mot de passe (si tu permets sa modif ici, sinon garde l'ancien)
        String mdp = motPasseField.getText().trim(); 
        // Note : Si ton écran ne permet pas de changer le MDP en modification, 
        // remets userExistant.getMotDePasse() ci-dessous.
        
        // 2. Gestion du POSTE depuis la ComboBox
        String selectionPoste = posteCombo.getValue();
        String codePoste = null;

        if (selectionPoste != null && !selectionPoste.isEmpty()) {
            // Extraction du code "1" de "1 - Admin"
            String[] parts = selectionPoste.split(" - ");
            if (parts.length > 0) {
                codePoste = parts[0].trim();
            }
        }

        // Seule vérification Java : s'assurer qu'un poste est sélectionné (car c'est une liste)
        if (codePoste == null || codePoste.isEmpty()) {
             afficherAlerte("Erreur", "Veuillez sélectionner un poste.", Alert.AlertType.WARNING);
             return;
        }

        try {
            // 3. Création de l'objet modifié
            Utilisateur userModifie = new Utilisateur(
                userExistant.getLogin(), // Le login ne change jamais en général
                mdp,                     
                nom, 
                prenom, 
                tel, 
                adresse, 
                ville, 
                codePoste
            );
            
            // IMPORTANT : On doit remettre l'ID technique pour que le WHERE SQL fonctionne !
            userModifie.setIdpers(userExistant.getIdpers());
            
            // 4. Appel au DAO
            boolean succes = utilisateurDAO.modifierUtilisateur(userModifie);

            if (succes) {
                afficherAlerte("Succès", "Utilisateur modifié avec succès !", Alert.AlertType.INFORMATION);
                chargerUtilisateurs(); // Rafraîchir le tableau
                resetFormulaire();     // Revenir en mode "Ajouter"
            }
            
        } catch (SQLException e) {
            // 5. Gestion PROPRE des erreurs Oracle
            String rawMessage = e.getMessage();
            String userMessage = rawMessage;

            // Nettoyage (retrait ORA-XXXX et pile technique)
            int deuxPoints = rawMessage.indexOf(": ");
            if (deuxPoints != -1) {
                userMessage = rawMessage.substring(deuxPoints + 2);
            }
            int finMessage = userMessage.indexOf("\n");
            if (finMessage != -1) {
                userMessage = userMessage.substring(0, finMessage);
            }
            
            afficherAlerte("Erreur modification", userMessage, Alert.AlertType.ERROR);
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur système", "Erreur inattendue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void supprimerUtilisateur(Utilisateur user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + user.getNomPrenom() + " ?", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if(utilisateurDAO.supprimerUtilisateur(user.getIdpers())) {
                    chargerUtilisateurs();
                }
            } catch (Exception e) {
                afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void clearForm() {
        loginField.clear();
        loginField.setEditable(true);
        motPasseField.clear();
        nomField.clear();
        prenomField.clear();
        adresseField.clear();
        villeField.clear();
        posteCombo.setValue(null); // Reset ComboBox
        telField.clear();
    }

    private void resetFormulaire() {
        clearForm();
        enregistrerButton.setText("Enregistrer");
        enregistrerButton.setOnAction(event -> handleEnregistrer());
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    

    @FXML
    private void versGestionCommandes(MouseEvent event) {
        try {
            System.out.println("🔄 Navigation vers GestionCommandes...");
            URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionCommandes.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent newRoot = loader.load();
            
            // ✅ CORRECTION : On caste vers GestionCommandesController
            // ET on crée le bon DAO (CommandeDAO)
            GestionCommandesController controller = loader.getController();
            
            if (this.utilisateurDAO != null) {
                // On récupère la connexion pour créer le DAO spécifique
                projetsgbd.DAO.CommandeDAO cmdDao = new projetsgbd.DAO.CommandeDAO(this.utilisateurDAO.getConnection());
                controller.initializeData(cmdDao);
            }

            Scene scene = new Scene(newRoot, 1100, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Commandes");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur Navigation", "Erreur Commandes : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void versGestionLivraisons(MouseEvent event) {
        try {
            System.out.println("🔄 Navigation vers GestionLivraisons...");
            URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionLivraisons.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent newRoot = loader.load();
            
            // ✅ CORRECTION : On caste vers GestionLivraisonsController
            // ET on crée le bon DAO (LivraisonDAO)
            GestionLivraisonsController controller = loader.getController();
            
            if (this.utilisateurDAO != null) {
                // On récupère la connexion pour créer le DAO spécifique
                projetsgbd.DAO.LivraisonDAO livDao = new projetsgbd.DAO.LivraisonDAO(this.utilisateurDAO.getConnection());
                controller.initializeData(livDao);
            }

            Scene scene = new Scene(newRoot, 1100, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Livraisons");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur Navigation", "Erreur Livraisons : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

@FXML
    private void versGestionUtilisateurs(MouseEvent event) {
        try {
            System.out.println("🔄 Navigation vers GestionUtilisateurs...");
            URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionUtilisateur.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent newRoot = loader.load();
            
            // ✅ CORRECTION : On caste vers GestionLivraisonsController
            // ET on crée le bon DAO (LivraisonDAO)
            GestionLivraisonsController controller = loader.getController();
            
            if (this.utilisateurDAO != null) {
                // On récupère la connexion pour créer le DAO spécifique
                projetsgbd.DAO.LivraisonDAO livDao = new projetsgbd.DAO.LivraisonDAO(this.utilisateurDAO.getConnection());
                controller.initializeData(livDao);
            }

            Scene scene = new Scene(newRoot, 1100, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Utilisateurs");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur Navigation", "Erreur Utilisateur : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
   
private String roleUtilisateur;

public void setRole(String role) {
    this.roleUtilisateur = role;

    switch (role) {
        case "magasinier":
            tabUsers.setVisible(false);
            tabLivraisons.setVisible(false);
            tabCommandes.setVisible(true);
            break;

        case "chef_livreur":
            tabUsers.setVisible(false);
            tabCommandes.setVisible(false);
            tabLivraisons.setVisible(true);
            break;

        case "admin":
            tabUsers.setVisible(true);
            tabCommandes.setVisible(true);
            tabLivraisons.setVisible(true);
            break;
    }
}
@FXML
private void handleSearch() {
    if (utilisateurDAO == null) return;

    String critere = searchField.getText();
    try {
        if (critere == null || critere.trim().isEmpty()) {
            // si champ vide → recharger tous les utilisateurs
            chargerUtilisateurs();
        } else {
            ObservableList<Utilisateur> result =
                    utilisateurDAO.chercherUtilisateurs(critere.trim());
            data.setAll(result);
        }
    } catch (Exception e) {
        e.printStackTrace();
        afficherAlerte("Erreur recherche", e.getMessage(), Alert.AlertType.ERROR);
    }
}

}
