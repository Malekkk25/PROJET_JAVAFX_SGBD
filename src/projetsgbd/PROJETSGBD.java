package projetsgbd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;

public class PROJETSGBD extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        File fxmlFile = new File("src/projetsgbd/View/Login.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
        
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1100, 600);
        
        primaryStage.setTitle("Système de Gestion des Livraisons");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
