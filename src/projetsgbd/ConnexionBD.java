package projetsgbd;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author poste
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBD {
 
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xE"; 

   
    public static Connection getConnection(String user, String password) throws SQLException {
        try {
          
            return DriverManager.getConnection(URL, user, password);
        } catch (SQLException e) {
            System.err.println("❌ Échec connexion Oracle pour : " + user);
            throw e; 
        }
    }
}

