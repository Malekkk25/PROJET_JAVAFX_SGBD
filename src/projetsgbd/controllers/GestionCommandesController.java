package projetsgbd.controllers;

import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import projetsgbd.DAO.CommandeDAO;
import projetsgbd.Model.Commande;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import projetsgbd.DAO.UtilisateurDAO;

public class GestionCommandesController {

    // --- CHAMPS FXML ---
    @FXML private ComboBox<String> cmbClient;
    @FXML private ComboBox<String> cmbArticle;
    @FXML private TextField txtQuantite;
    @FXML private DatePicker dpDateCommande;
    
    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, Number> colNumero;
    @FXML private TableColumn<Commande, String> colClient;
    @FXML private TableColumn<Commande, String> colDate;
    @FXML private TableColumn<Commande, String> colEtat;
    @FXML private TableColumn<Commande, Number> colMontant;
    @FXML private TableColumn<Commande, Void> colActions;
    @FXML private HBox tabUsers;
@FXML private HBox tabCommandes;
@FXML private HBox tabLivraisons;
@FXML private TextField searchField;

    @FXML private Label lblCommandesCount;

    private CommandeDAO commandeDAO;
    private final ObservableList<Commande> dataCommandes = FXCollections.observableArrayList();
    private final Map<Integer, Integer> panier = new HashMap<>(); 

    public void initializeData(CommandeDAO dao) {
        
        this.commandeDAO = dao;
        chargerListesDeroulantes();
        refreshData();
    }

    @FXML
    private void initialize() {
        // Colonnes
        colNumero.setCellValueFactory(cell -> cell.getValue().nocdeProperty());
        colClient.setCellValueFactory(cell -> cell.getValue().nomClientProperty());
        colDate.setCellValueFactory(cell -> cell.getValue().dateCdeProperty().asString());
        colEtat.setCellValueFactory(cell -> cell.getValue().etatCdeProperty());
        colMontant.setCellValueFactory(cell -> cell.getValue().montantTotalProperty());

        // --- COLONNE ACTIONS (Menu Complet) ---
        colActions.setCellFactory(col -> new TableCell<Commande, Void>() {
            private final Button btnAnnuler = new Button("✖");
            private final MenuButton menuEtat = new MenuButton("Etat");
            
            {
                // Bouton Annuler
                btnAnnuler.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 25px;");
                btnAnnuler.setTooltip(new Tooltip("Annuler la commande"));
                btnAnnuler.setOnAction(e -> {
                    Commande c = getTableView().getItems().get(getIndex());
                    annulerCommande(c);
                });

                // Menu Etats
                menuEtat.setStyle("-fx-background-color: #4c6fff; -fx-text-fill: white; -fx-font-size: 10px;");
                
                MenuItem itemPr = new MenuItem("PR (Prête)");
                MenuItem itemLi = new MenuItem("LI (Livrée)"); 
                MenuItem itemSo = new MenuItem("SO (Soldée)");
                
                // ✅ AJOUT DE "AL"
                MenuItem itemAl = new MenuItem("AL (Anomalie)");
                itemAl.setStyle("-fx-text-fill: red; font-weight: bold;");

                // Actions
                itemPr.setOnAction(e -> changerEtat(getTableView().getItems().get(getIndex()), "PR"));
                itemLi.setOnAction(e -> changerEtat(getTableView().getItems().get(getIndex()), "LI"));
                itemSo.setOnAction(e -> changerEtat(getTableView().getItems().get(getIndex()), "SO"));
                itemAl.setOnAction(e -> changerEtat(getTableView().getItems().get(getIndex()), "AL"));
                
                menuEtat.getItems().addAll(itemPr, itemLi, itemSo, itemAl);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Commande c = getTableView().getItems().get(getIndex());
                    
                    // Si AN, SO ou AL (états finaux ou bloquants), on masque les actions
                    if ("AN".equals(c.getEtatCde()) || "SO".equals(c.getEtatCde()) || "AL".equals(c.getEtatCde())) {
                        setGraphic(null); 
                    } else {
                        HBox box = new HBox(5, menuEtat, btnAnnuler);
                        setGraphic(box);
                    }
                }
            }
        });

        tableCommandes.setItems(dataCommandes);
        
        if(dpDateCommande != null) {
             dpDateCommande.setValue(LocalDate.now());
             dpDateCommande.setDisable(true);
        }
        
    }

    private void chargerListesDeroulantes() {
        if (commandeDAO == null) return;
        try {
            cmbClient.setItems(commandeDAO.chargerClients());
            cmbArticle.setItems(commandeDAO.chargerArticles());
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les listes : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshData() {
        if (commandeDAO == null) return;
        try {
            dataCommandes.setAll(commandeDAO.chargerToutesCommandes());
            if(lblCommandesCount != null) {
                lblCommandesCount.setText(dataCommandes.size() + " commandes");
            }
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les commandes : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAjouterArticle() {
        String selectionArticle = cmbArticle.getValue();
        String qteStr = txtQuantite.getText();

        if (selectionArticle == null || qteStr.isEmpty()) {
            afficherAlerte("Attention", "Sélectionnez un article et une quantité.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idArticle = Integer.parseInt(selectionArticle.split(" - ")[0]);
            int qte = Integer.parseInt(qteStr);
            
            if (qte <= 0) {
                afficherAlerte("Erreur", "La quantité doit être positive.", Alert.AlertType.WARNING);
                return;
            }

            panier.put(idArticle, panier.getOrDefault(idArticle, 0) + qte);
            afficherAlerte("Panier", "Article ajouté ! (Total types : " + panier.size() + ")", Alert.AlertType.INFORMATION);
            
            txtQuantite.clear();
            cmbArticle.setValue(null);
            
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Quantité invalide.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEnregistrer() {
        String selectionClient = cmbClient.getValue();
        if (selectionClient == null || panier.isEmpty()) {
            afficherAlerte("Erreur", "Sélectionnez un client et ajoutez des articles.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idClient = Integer.parseInt(selectionClient.split(" - ")[0]);
            commandeDAO.creerCommandeComplete(idClient, panier);
            
            afficherAlerte("Succès", "Commande créée avec succès !", Alert.AlertType.INFORMATION);
            panier.clear();
            refreshData();
            
        } catch (Exception e) {
            afficherAlerte("Erreur Création", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void annulerCommande(Commande c) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Annuler la commande " + c.getNocde() + " ?", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                commandeDAO.annulerCommande(c.getNocde());
                refreshData();
                afficherAlerte("Succès", "Commande annulée.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                traiterErreurOracle(e);
            }
        }
    }

    private void changerEtat(Commande c, String nouvelEtat) {
        try {
            commandeDAO.modifierEtatCommande(c.getNocde(), nouvelEtat);
            afficherAlerte("Succès", "État modifié en " + nouvelEtat, Alert.AlertType.INFORMATION);
            refreshData();
        } catch (SQLException e) {
            traiterErreurOracle(e);
        }
    }

    private void traiterErreurOracle(SQLException e) {
        String msg = e.getMessage();
        if (msg.contains("ORA-")) {
            int start = msg.indexOf("ORA-");
            msg = msg.substring(start);
        }
        afficherAlerte("Erreur Base de Données", msg, Alert.AlertType.ERROR);
    }

private void afficherAlerte(String titre, String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(titre);
    alert.setHeaderText(null);
    alert.setContentText(message);

    // AVANT : alert.showAndWait();
    alert.show();  // non bloquant → ne casse pas l’animation/layout
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

        // ✅ bon type de contrôleur
        GestionUtilisateursController controller = loader.getController();

        if (this.commandeDAO != null) {
            UtilisateurDAO userDao = new UtilisateurDAO(this.commandeDAO.getConnection());
            controller.initializeData(userDao);
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

@FXML
private void handleSearch() {
    String critere = (searchField.getText() == null) ? "" : searchField.getText().trim().toUpperCase();

    if (critere.isEmpty()) {
        // recharger toutes les commandes depuis la BD
        refreshData();
        return;
    }

    // filtre en mémoire sur dataCommandes
    ObservableList<Commande> filtered = FXCollections.observableArrayList();
    for (Commande c : dataCommandes) {
        String num = String.valueOf(c.getNocde());
        String client = c.getNomClient() != null ? c.getNomClient().toUpperCase() : "";
        if (num.contains(critere) || client.contains(critere)) {
            filtered.add(c);
        }
    }
    tableCommandes.setItems(filtered);
}

}
