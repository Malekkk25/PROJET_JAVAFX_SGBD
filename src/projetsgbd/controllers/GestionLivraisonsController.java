package projetsgbd.controllers;

import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import projetsgbd.DAO.LivraisonDAO;
import projetsgbd.Model.Livraison;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import projetsgbd.DAO.UtilisateurDAO;
import projetsgbd.Model.Utilisateur;

public class GestionLivraisonsController {

    // Formulaire
    @FXML private ComboBox<String> cmbCommande;
    @FXML private ComboBox<String> cmbLivreur;
    @FXML private DatePicker dpDate;
    @FXML private Button btnPlanifier;

    // Tableau
    @FXML private Label lblLivraisonsCount;
    @FXML private TableView<Livraison> tableLivraisons;
    @FXML private TableColumn<Livraison, Number> colNumero;
    @FXML private TableColumn<Livraison, String> colClientVille;
    @FXML private TableColumn<Livraison, String> colDateLivraison;
    @FXML private TableColumn<Livraison, String> colLivreur;
    @FXML private TableColumn<Livraison, String> colModePaiement;
    @FXML private TableColumn<Livraison, String> colEtat;
    @FXML private TableColumn<Livraison, Void> colActions;
@FXML private ComboBox<String> cmbModePaiement;
private String ancienLivreurId;
private LocalDate ancienneDate;
@FXML private HBox tabUsers;
@FXML private HBox tabCommandes;
@FXML private HBox tabLivraisons;
@FXML
private TextField searchField;
    private final ObservableList<Livraison> dataLivraisons = FXCollections.observableArrayList();
    private LivraisonDAO livraisonDAO;
    private boolean modeModification = false;

    @FXML
    private void initialize() {
        // Colonnes
        colNumero.setCellValueFactory(cd -> cd.getValue().nocdeProperty());
        colClientVille.setCellValueFactory(cd -> cd.getValue().villeClientProperty());
        colDateLivraison.setCellValueFactory(cd -> cd.getValue().dateLivProperty().asString());
        colLivreur.setCellValueFactory(cd -> cd.getValue().livreurProperty());
        colModePaiement.setCellValueFactory(cd -> cd.getValue().modePayProperty());
        colEtat.setCellValueFactory(cd -> cd.getValue().etatLivProperty());

        // Colonne actions
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");

            {
                btnEdit.setStyle("-fx-background-color: #4c6fff; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnDelete.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");

                btnEdit.setOnAction(event -> {
                    Livraison liv = getTableView().getItems().get(getIndex());
                    chargerLivraisonDansFormulaire(liv);
                });

                btnDelete.setOnAction(event -> {
                    Livraison liv = getTableView().getItems().get(getIndex());
                    supprimerLivraison(liv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });

        tableLivraisons.setItems(dataLivraisons);
        cmbModePaiement.getItems().setAll("avant_livraison", "apres_livraison");
    }

    // Appelé depuis le Main
    public void initializeData(LivraisonDAO dao) {
        this.livraisonDAO = dao;
        refreshAllData();
    }

    private void refreshAllData() {
        chargerTableauLivraisons();
        chargerCombos();
    }

    private void chargerTableauLivraisons() {
        if (livraisonDAO == null) return;
        try {
            ObservableList<Livraison> list = livraisonDAO.chargerToutesLivraisons();
            dataLivraisons.setAll(list);
            lblLivraisonsCount.setText(list.size() + " livraisons");
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur Chargement", "Impossible de charger les livraisons.", Alert.AlertType.ERROR);
        }
    }

    private void chargerCombos() {
        if (livraisonDAO == null) return;
        try {
            String currentCmd = cmbCommande.getValue();
            ObservableList<String> cdes = livraisonDAO.chargerCommandesEligibles();

            if (modeModification && currentCmd != null && !cdes.contains(currentCmd)) {
                cdes.add(0, currentCmd);
            }

            cmbCommande.setItems(cdes);
            cmbLivreur.setItems(livraisonDAO.chargerLivreurs());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

@FXML
private void handlePlanifier() {
    if (cmbCommande.getValue() == null ||
        cmbLivreur.getValue() == null ||
        dpDate.getValue() == null ||
        cmbModePaiement.getValue() == null) {
        afficherAlerte("Validation",
                "Remplissez commande, livreur, date et mode de paiement.",
                Alert.AlertType.WARNING);
        return;
    }

    try {
        String commandeStr = cmbCommande.getValue();
        int nocde = Integer.parseInt(commandeStr.split(" - ")[0].trim());

        String livreurStr = cmbLivreur.getValue();
        String idLivreur = livreurStr.split(" - ")[0].trim();

        LocalDate nouvelleDate = dpDate.getValue();

        // En modification : on n’autorise que la date OU le livreur à changer (pas les deux)
        if (modeModification) {
            boolean dateChangee = (ancienneDate != null) && !nouvelleDate.equals(ancienneDate);
            boolean livreurChange = (ancienLivreurId != null) && !idLivreur.equals(ancienLivreurId);

            if (dateChangee && livreurChange) {
                afficherAlerte(
                        "Règle de gestion",
                        "En modification, vous ne pouvez changer que la date OU le livreur, pas les deux.",
                        Alert.AlertType.WARNING
                );
                return;
            }
        }

        Livraison liv = new Livraison();
        liv.setNocde(nocde);
        liv.setLivreur(idLivreur);
        liv.setDateLiv(Date.valueOf(nouvelleDate));
        liv.setModePay(cmbModePaiement.getValue());

        if (modeModification) {
            livraisonDAO.modifierLivraison(liv);
            afficherAlerte("Succès", "Livraison modifiée !", Alert.AlertType.INFORMATION);
        } else {
            livraisonDAO.ajouterLivraison(liv);
            afficherAlerte("Succès", "Livraison planifiée !", Alert.AlertType.INFORMATION);
        }

        resetFormulaire();
        refreshAllData();

    } catch (NumberFormatException e) {
        afficherAlerte("Erreur", "Format commande ou livreur invalide.", Alert.AlertType.ERROR);
    } catch (SQLException e) {
        traiterErreurOracle(e);
    } catch (Exception e) {
        afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
    }
}


  private void chargerLivraisonDansFormulaire(Livraison liv) {
    modeModification = true;
    btnPlanifier.setText("✏️ Modifier");

    String cmdItem = liv.getNocde() + " - (Actuelle)";
    if (!cmbCommande.getItems().contains(cmdItem)) {
        cmbCommande.getItems().add(0, cmdItem);
    }
    cmbCommande.setValue(cmdItem);
    cmbCommande.setDisable(true);

    // mémoriser anciennes valeurs
    ancienLivreurId = liv.getLivreur();                     // id (String)
    if (liv.getDateLiv() != null) {
        ancienneDate = ((Date) liv.getDateLiv()).toLocalDate();
        dpDate.setValue(ancienneDate);
    }

    chargerNomLivreurPourEdition(ancienLivreurId);
}



    private void chargerNomLivreurPourEdition(String matriculeStr) {
    if (livraisonDAO == null || matriculeStr == null || matriculeStr.isEmpty()) return;
    try {
        int mat = Integer.parseInt(matriculeStr);
        String nomComplet = livraisonDAO.getNomLivreurById(mat);
        String itemLiv = mat + " - " + nomComplet;
        if (!cmbLivreur.getItems().contains(itemLiv)) {
            cmbLivreur.getItems().add(0, itemLiv);
        }
        cmbLivreur.setValue(itemLiv);
    } catch (Exception e) {
        System.err.println("Erreur chargement livreur: " + e.getMessage());
    }
}


    private void supprimerLivraison(Livraison liv) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la livraison de la commande " + liv.getNocde() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.OK) {
            try {
                livraisonDAO.supprimerLivraison(liv.getNocde());
                refreshAllData();
            } catch (SQLException e) {
                traiterErreurOracle(e);
            }
        }
    }

    private void resetFormulaire() {
        cmbCommande.setValue(null);
        cmbCommande.setDisable(false);
        cmbLivreur.setValue(null);
        dpDate.setValue(null);
        btnPlanifier.setText("📅  Planifier Livraison");
        modeModification = false;
        cmbModePaiement.setValue(null);

        chargerCombos();
    }

    private void traiterErreurOracle(SQLException e) {
        String rawMessage = e.getMessage();
        String userMessage = rawMessage;

        if (rawMessage.contains("ORA-20")) {
            int separator = rawMessage.indexOf(": ");
            if (separator != -1) {
                userMessage = rawMessage.substring(separator + 2);
            }
            int stackTraceStart = userMessage.indexOf("ORA-");
            if (stackTraceStart != -1) {
                userMessage = userMessage.substring(0, stackTraceStart).trim();
            }
            if (userMessage.contains("\n")) {
                userMessage = userMessage.substring(0, userMessage.indexOf("\n")).trim();
            }
            afficherAlerte("Règle de gestion", userMessage, Alert.AlertType.WARNING);
        } else {
            afficherAlerte("Erreur Base de Données", "Erreur inattendue : " + rawMessage, Alert.AlertType.ERROR);
        }
    }

    private void afficherAlerte(String titre, String contenu, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.show();
    }
    
    
    

@FXML
private void versGestionCommandes(MouseEvent event) {
    try {
        System.out.println("🔄 Navigation vers GestionCommandes...");
        URL fxmlUrl = getClass().getResource("/projetsgbd/View/GestionCommandes.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent newRoot = loader.load();

        GestionCommandesController controller = loader.getController();

        // ✅ recréer CommandeDAO avec la même connexion (via SessionManager)
        projetsgbd.DAO.CommandeDAO cmdDao =
                new projetsgbd.DAO.CommandeDAO(projetsgbd.Model.SessionManager.getConnection());
        controller.initializeData(cmdDao);

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

        GestionLivraisonsController controller = loader.getController();

        // ✅ recréer LivraisonDAO avec la même connexion
        LivraisonDAO livDao =
                new LivraisonDAO(projetsgbd.Model.SessionManager.getConnection());
        controller.initializeData(livDao);

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

        GestionUtilisateursController controller = loader.getController();

        // ✅ recréer UtilisateurDAO avec la même connexion
        UtilisateurDAO userDao =
                new UtilisateurDAO(projetsgbd.Model.SessionManager.getConnection());
        controller.initializeData(userDao);

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
    String critere = (searchField.getText() == null)
            ? ""
            : searchField.getText().trim().toUpperCase();

    if (critere.isEmpty()) {
        // recharger toutes les livraisons depuis la BD
        refreshAllData();              // ou chargerTableauLivraisons();
        return;
    }

    ObservableList<Livraison> filtered = FXCollections.observableArrayList();

    for (Livraison l : dataLivraisons) {
        String num    = String.valueOf(l.getNocde());
        String livreur = l.getLivreur()      != null ? l.getLivreur().toUpperCase()      : "";
        String ville   = l.getVilleClient()  != null ? l.getVilleClient().toUpperCase()  : "";
        String dateStr = (l.getDateLiv()     != null ? l.getDateLiv().toString() : "").toUpperCase();

        if (num.contains(critere)
                || livreur.contains(critere)
                || ville.contains(critere)
                || dateStr.contains(critere)) {
            filtered.add(l);
        }
    }

    tableLivraisons.setItems(filtered);
    lblLivraisonsCount.setText(filtered.size() + " livraisons");
}
}
