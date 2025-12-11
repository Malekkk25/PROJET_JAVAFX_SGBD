package projetsgbd.DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import oracle.jdbc.OracleTypes;
import projetsgbd.Model.Livraison;

import java.sql.*;

public class LivraisonDAO {
    private final Connection connection;

    public LivraisonDAO(Connection connection) {
        this.connection = connection;
    }


    // Indispensable pour la navigation entre contrôleurs
    public Connection getConnection() {
        return this.connection;
    }
   // AjouterLivraison
public boolean ajouterLivraison(Livraison liv) throws SQLException {
    String plsqlCall = "{call PKG_Livraisons.AjouterLivraison(?, ?, ?, ?)}";
    try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
        cstmt.setInt(1, liv.getNocde());
        cstmt.setDate(2, new java.sql.Date(liv.getDateLiv().getTime()));
        cstmt.setInt(3, Integer.parseInt(liv.getLivreur())); // id livreur (NUMBER)
        cstmt.setString(4, liv.getModePay());
        cstmt.execute();
        return true;
    }
}


// ModifierLivraison
public boolean modifierLivraison(Livraison liv) throws SQLException {
    String plsqlCall = "{call PKG_Livraisons.ModifierLivraison(?, ?, ?)}";
    try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
        cstmt.setInt(1, liv.getNocde());
        cstmt.setDate(2, new java.sql.Date(liv.getDateLiv().getTime()));
        cstmt.setInt(3, Integer.parseInt(liv.getLivreur())); // id livreur
        cstmt.execute();
        return true;
    }
}



    // Supprimer
    public boolean supprimerLivraison(int noCommande) throws SQLException {
        String plsqlCall = "{call PKG_Livraisons.SupprimerLivraison(?)}";
        try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
            cstmt.setInt(1, noCommande);
            cstmt.execute();
            return true;
        }
    }

  public ObservableList<Livraison> chargerToutesLivraisons() throws SQLException {
    ObservableList<Livraison> list = FXCollections.observableArrayList();

    try (CallableStatement cstmt = connection.prepareCall(
            "{ ? = call PROJETSGBD.PKG_LIVRAISONS.ChercherLivraison(?, ?, ?, ?) }")) {

        cstmt.registerOutParameter(1, OracleTypes.CURSOR);
        cstmt.setNull(2, Types.INTEGER); // pnocde
        cstmt.setNull(3, Types.INTEGER); // plivreur
        cstmt.setNull(4, Types.VARCHAR); // pville
        cstmt.setNull(5, Types.DATE);    // pdate
        cstmt.execute();

        try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
            while (rs.next()) {
                Livraison l = new Livraison();
                l.setNocde(rs.getInt("NOCDE"));
                l.setVilleClient(rs.getString("VILLECLT"));
                l.setDateLiv(rs.getDate("DATELIV"));
                l.setLivreur(rs.getString("LIVREUR"));
                l.setModePay(rs.getString("MODEPAY"));
                // ⚠️ ici il faut ETAT, pas ETATLIV
                l.setEtatLiv(rs.getString("ETAT"));
                list.add(l);
            }
        }
    }
    return list;
}


    // Commandes éligibles
    public ObservableList<String> chargerCommandesEligibles() throws SQLException {
        ObservableList<String> commandes = FXCollections.observableArrayList();
        String sql = """
                SELECT NOCDE, DATECDE 
                FROM COMMANDES 
                WHERE ETATCDE IN ('PR', 'EC')
                  AND NOCDE NOT IN (SELECT NOCDE FROM LIVRAISONCOM)
                ORDER BY NOCDE DESC
                """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                commandes.add(rs.getInt("NOCDE") + " - Date: " + rs.getDate("DATECDE"));
            }
        }
        return commandes;
    }

// Dans LivraisonDAO

public ObservableList<String> chargerLivreurs() throws SQLException {
    ObservableList<String> livreurs = FXCollections.observableArrayList();
    String sql = """
        SELECT IDPERS, NOMPERS, PRENOMPERS
        FROM V_Livreurs
        ORDER BY IDPERS
    """;
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            livreurs.add(rs.getInt("IDPERS") + " - " +
                         rs.getString("NOMPERS") + " " +
                         rs.getString("PRENOMPERS"));
        }
    }
    return livreurs;
}




// Pour l’édition, récupérer le nom via PERSONNEL
public String getNomLivreurById(int matricule) throws SQLException {
    String sql = "SELECT NOM || ' ' || PRENOM AS NOM_COMPLET FROM PERSONNEL WHERE MATRICULE = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, matricule);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getString("NOM_COMPLET") : "Livreur inconnu";
        }
    }
}

}