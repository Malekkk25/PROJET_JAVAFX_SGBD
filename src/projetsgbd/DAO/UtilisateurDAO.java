package projetsgbd.DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import projetsgbd.Model.Utilisateur;
import java.sql.*;

/**
 * DAO pour les Utilisateurs - VERSION CORRIGÉE
 * @author poste
 */
public class UtilisateurDAO {
    private Connection connection;

    public UtilisateurDAO(Connection connection) {
        this.connection = connection;
    }
    
    // =========================================================================
    // 1. GESTION DES UTILISATEURS (TABLE PERSONNEL)
    // =========================================================================
    
    public boolean ajouterUtilisateur(Utilisateur user) throws Exception {
        String plsqlCall = "{call PKG_Utilisateurs.Ajouter_Utilisateur(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
            cstmt.setString(1, user.getNom());
            cstmt.setString(2, user.getPrenom());
            cstmt.setString(3, user.getAdresse());
            cstmt.setString(4, user.getVille());
            cstmt.setString(5, user.getTelephone());
            cstmt.setDate(6, new java.sql.Date(System.currentTimeMillis())); 
            cstmt.setString(7, user.getLogin());
            cstmt.setString(8, user.getMotDePasse());
            cstmt.setString(9, user.getPoste());  
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur Oracle : " + e.getMessage());
            throw e;
        }
    }

    public boolean modifierUtilisateur(Utilisateur user) throws Exception {
        String plsqlCall = "{call PKG_Utilisateurs.Modifier_Utilisateur(?, ?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
            cstmt.setInt(1, user.getIdpers()); 
            cstmt.setString(2, user.getNom());
            cstmt.setString(3, user.getPrenom());
            cstmt.setString(4, user.getAdresse());
            cstmt.setString(5, user.getVille());
            cstmt.setString(6, user.getTelephone());
            cstmt.setString(7, user.getPoste()); 
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur Oracle Modification : " + e.getMessage());
            throw e;
        }
    }

    public boolean supprimerUtilisateur(int idpers) throws Exception {
        String plsqlCall = "{call PKG_Utilisateurs.Supprimer_Utilisateur(?)}";
        try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
            cstmt.setInt(1, idpers);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            throw e;
        }
    }

    // =========================================================================
    // 2. LECTURE ET RECHERCHE
    // =========================================================================

    public ObservableList<Utilisateur> chargerTousLesUtilisateurs() throws Exception {
    ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    String sql = """
        SELECT IDPERS, NOMPERS, PRENOMPERS,
               ADRPERS, VILLEPERS, TELPERS,
               D_EMBAUCHE, LOGIN, CODEPOSTE
        FROM PERSONNEL
        ORDER BY IDPERS
    """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            Utilisateur user = new Utilisateur(
                    rs.getString("LOGIN"),              // login
                    "",                                 // mot de passe (non lu ici)
                    rs.getString("NOMPERS"),           // nom
                    rs.getString("PRENOMPERS"),        // prénom
                    rs.getString("TELPERS"),           // téléphone
                    rs.getString("ADRPERS"),           // adresse
                    rs.getString("VILLEPERS"),         // ville
                    rs.getString("CODEPOSTE")          // code poste (1,2,3,4…)
            );
            user.setIdpers(rs.getInt("IDPERS"));

            Date dateEmbauche = rs.getDate("D_EMBAUCHE");
            if (dateEmbauche != null) {
                user.setDateEmbauche(dateEmbauche.toString());
            }

            utilisateurs.add(user);
        }
    } catch (SQLException e) {
        System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
        throw e;
    }
    return utilisateurs;
}


    public ObservableList<Utilisateur> chercherUtilisateurs(String critere) throws Exception {
        ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
        String sql = "SELECT IDPERS, NOMPERS, PRENOMPERS, ADRPERS, VILLEPERS, TELPERS, D_EMBAUCHE, LOGIN, CODEPOSTE " +
                     "FROM PERSONNEL WHERE UPPER(NOMPERS) LIKE ? OR UPPER(LOGIN) LIKE ? ORDER BY IDPERS";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + critere.toUpperCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Utilisateur user = new Utilisateur(
                            rs.getString("LOGIN"), "", rs.getString("NOMPERS"),
                            rs.getString("PRENOMPERS"), rs.getString("TELPERS"),
                            rs.getString("ADRPERS"), rs.getString("VILLEPERS"),
                            rs.getString("CODEPOSTE")
                    );
                    user.setIdpers(rs.getInt("IDPERS"));
                    Date dateEmbauche = rs.getDate("D_EMBAUCHE");
                    if (dateEmbauche != null) {
                        user.setDateEmbauche(dateEmbauche.toString());
                    }
                    utilisateurs.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            throw e;
        }
        return utilisateurs;
    }

    public ObservableList<String> chargerListePostes() throws SQLException {
        ObservableList<String> postes = FXCollections.observableArrayList();
        String sql = "SELECT CODEPOSTE, LIBELLE FROM POSTES ORDER BY CODEPOSTE"; 
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String code = rs.getString("CODEPOSTE");
                String libelle = rs.getString("LIBELLE");
                if (code != null && libelle != null) {
                    postes.add(code + " - " + libelle);
                }
            }
        }
        return postes;
    }

    public int compterUtilisateurs() throws Exception {
        String sql = "SELECT COUNT(*) FROM PERSONNEL";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    // =========================================================================
    // 3. GESTION DES PRIVILÈGES (PACKAGES)
    // =========================================================================

    /**
     * ✅ Nouvelle méthode unifiée : Appelle Attribuer_Role(login, role, password)
     * Le package s'occupe de créer le user s'il n'existe pas.
     */
public String grantUser(String login, String codePoste, String motDePasse) throws SQLException {
    // Nettoyage côté Java
    String loginNorm = login.trim().toUpperCase();   // comme v_login en PL/SQL
    String pwdNorm   = motDePasse.trim();            // comme v_pwd

    String nomRoleOracle = getNomRoleFromCode(codePoste);
    if (nomRoleOracle == null) {
        throw new SQLException("Rôle inconnu pour le code poste " + codePoste);
    }

    String callCreate = "{call PKG_Privileges.Creer_Compte_Oracle(?, ?)}";
    String callGrant  = "{call PKG_Privileges.Attribuer_Role(?, ?, ?)}";

    try (CallableStatement cCreate = connection.prepareCall(callCreate);
         CallableStatement cGrant  = connection.prepareCall(callGrant)) {

        cCreate.setString(1, loginNorm);
        cCreate.setString(2, pwdNorm);
        cCreate.execute();

        cGrant.setString(1, loginNorm);
        cGrant.setString(2, nomRoleOracle);
        cGrant.setString(3, pwdNorm);
        cGrant.execute();
System.out.println(loginNorm +"----------"+pwdNorm);
        return "Compte Oracle créé et privilèges attribués avec succès.";
    }
}




    // =========================================================================
    // 4. UTILITAIRES
    // =========================================================================

    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    public Connection getConnection() {
        return this.connection;
    }
        
    private String getNomRoleFromCode(String codePoste) {
        if (codePoste == null) return null;
        
        switch (codePoste.trim()) {
            case "1": return "ADMINISTRATEUR"; 
            case "2": return "MAGASINIER";     
            case "3": return "CHEFLIVREUR";    
            default: 
                System.err.println("Attention : Code poste " + codePoste + " non mappé.");
                return null;
        }
    }
     public String initialiserVues() throws SQLException {
        String plsqlCall = "{call PKG_Privileges.Creer_Schemas_Externes}";
        try (CallableStatement cstmt = connection.prepareCall(plsqlCall)) {
            cstmt.execute();
            return "Vues créées avec succès.";
        } catch (SQLException e) {
            System.err.println("Erreur Init Vues : " + e.getMessage());
            throw e;
        }
    }
     public String getCodePosteByLogin(String login) throws SQLException {
    String sql = "SELECT CODEPOSTE FROM V_Mon_Profil";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getString("CODEPOSTE");
        }
    }
    return null;
}
     public String getMotDePasseByLogin(String login) throws SQLException {
    String sql = "SELECT MOTP FROM PERSONNEL WHERE LOGIN = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, login);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("MOTP");
            }
        }
    }
    return null;
}


}
