package projetsgbd.DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import projetsgbd.Model.Commande;
import java.sql.*;
import java.util.Map;
import oracle.jdbc.OracleTypes;

public class CommandeDAO {
    private Connection connection;

    public CommandeDAO(Connection connection) {
        this.connection = connection;
    }
    
    // Indispensable pour la navigation entre contrôleurs
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Charge toutes les commandes via le CURSEUR du package
     */
       public ObservableList<Commande> chargerToutesCommandes() throws SQLException {
        ObservableList<Commande> list = FXCollections.observableArrayList();
        String plsql = "{? = call PKG_Commandes.ChercherCommande(?, ?, ?)}";

        try (CallableStatement cstmt = connection.prepareCall(plsql)) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setNull(2, Types.INTEGER);
            cstmt.setNull(3, Types.INTEGER);
            cstmt.setNull(4, Types.DATE);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    int nocde = rs.getInt("NOCDE");
                    int noclt = rs.getInt("NOCLT");
                    Date dateCde = rs.getDate("DATECDE");
                    String etat = rs.getString("ETATCDE");

                    // CORRECTION : On lit directement les colonnes ajoutées dans la requête
                    String nomClient = rs.getString("NOMCLT"); 
                    double montant = rs.getDouble("MONTANT");

                    list.add(new Commande(nocde, noclt, nomClient, dateCde, etat, montant));
                }
            }
        }
        return list;
    }


    /**
     * Crée une commande COMPLETE (Entête + Lignes) via le Package
     */
    public void creerCommandeComplete(int noClient, Map<Integer, Integer> panierArticles) throws Exception {
        try {
            connection.setAutoCommit(false); // Début Transaction

            int noCdeGenere = 0;

            // 1. CRÉATION ENTÊTE : Appel AjouterCommande(p_noclt, OUT p_new_nocde)
            String plsqlEntete = "{call PKG_Commandes.AjouterCommande(?, ?)}";
            try (CallableStatement cstmt = connection.prepareCall(plsqlEntete)) {
                cstmt.setInt(1, noClient);
                cstmt.registerOutParameter(2, Types.INTEGER); // On récupère l'ID généré
                cstmt.execute();
                
                noCdeGenere = cstmt.getInt(2); // Récupération de l'ID
            }

            // 2. CRÉATION LIGNES : Appel AjouterLigneCommande(p_nocde, p_refart, p_qte)
            // On boucle sur le panier Java pour appeler la procédure pour chaque article
            String plsqlLigne = "{call PKG_Commandes.AjouterLigneCommande(?, ?, ?)}";
            try (CallableStatement cstmt = connection.prepareCall(plsqlLigne)) {
                for (Map.Entry<Integer, Integer> entry : panierArticles.entrySet()) {
                    cstmt.setInt(1, noCdeGenere);      // ID de la commande qu'on vient de créer
                    cstmt.setInt(2, entry.getKey());   // ID Article (RefArt)
                    cstmt.setInt(3, entry.getValue()); // Quantité
                    cstmt.execute(); 
                }
            }

            connection.commit(); // Valider la transaction si tout est OK
            
        } catch (Exception e) {
            connection.rollback(); // Annuler si erreur
            System.err.println("Erreur transaction commande : " + e.getMessage());
            throw e; // Relancer pour que le Contrôleur affiche l'alerte
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void modifierEtatCommande(int nocde, String nouvelEtat) throws SQLException {
        String plsql = "{call PKG_Commandes.ModifierEtatCommande(?, ?)}";
        try (CallableStatement cstmt = connection.prepareCall(plsql)) {
            cstmt.setInt(1, nocde);
            cstmt.setString(2, nouvelEtat);
            cstmt.execute();
        }
    }
    public void annulerCommande(int nocde) throws SQLException {
        String plsql = "{call PKG_Commandes.AnnulerCommande(?)}";
        try (CallableStatement cstmt = connection.prepareCall(plsql)) {
            cstmt.setInt(1, nocde);
            cstmt.execute();
        }
    }

    // ================= HELPER METHODS (SQL Direct pour UI) =================

    private String getNomClient(int noclt) {
        String sql = "SELECT NOMCLT FROM CLIENTS WHERE NOCLT = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, noclt);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("NOMCLT");
            }
        } catch (SQLException e) { /* Ignore */ }
        return "Client " + noclt;
    }



    public ObservableList<String> chargerClients() throws SQLException {
        ObservableList<String> clients = FXCollections.observableArrayList();
        String sql = "SELECT NOCLT, NOMCLT FROM CLIENTS ORDER BY NOCLT ";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(rs.getInt("NOCLT") + " - " + rs.getString("NOMCLT"));
            }
        }
        return clients;
    }

    public ObservableList<String> chargerArticles() throws SQLException {
        ObservableList<String> articles = FXCollections.observableArrayList();
        String sql = "SELECT REFART, DESIGNATION, PRIX FROM ARTICLES ORDER BY REFART";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                articles.add(rs.getInt("REFART") + " - " + rs.getString("DESIGNATION") + " (" + rs.getDouble("PRIX") + " DT)");
            }
        }
        return articles;
    }
}
