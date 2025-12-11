package projetsgbd.Model;

import javafx.beans.property.*;
import java.util.Date;

public class Livraison {

    private final IntegerProperty nocde = new SimpleIntegerProperty();
    private final StringProperty villeClient = new SimpleStringProperty();
    private final ObjectProperty<Date> dateLiv = new SimpleObjectProperty<>();
    private final StringProperty livreur = new SimpleStringProperty();      // pour affichage "1 - Trabelsi" ou nom
    private final StringProperty modePay = new SimpleStringProperty();
    private final StringProperty etatLiv = new SimpleStringProperty();

    // NOCDE
    public int getNocde() { return nocde.get(); }
    public void setNocde(int value) { nocde.set(value); }
    public IntegerProperty nocdeProperty() { return nocde; }

    // Ville client
    public String getVilleClient() { return villeClient.get(); }
    public void setVilleClient(String value) { villeClient.set(value); }
    public StringProperty villeClientProperty() { return villeClient; }

    // Date livraison
    public Date getDateLiv() { return dateLiv.get(); }
    public void setDateLiv(Date value) { dateLiv.set(value); }
    public ObjectProperty<Date> dateLivProperty() { return dateLiv; }

    // Livreur (affichage)
    public String getLivreur() { return livreur.get(); }
    public void setLivreur(String value) { livreur.set(value); }
    public StringProperty livreurProperty() { return livreur; }

    // Mode paiement
    public String getModePay() { return modePay.get(); }
    public void setModePay(String value) { modePay.set(value); }
    public StringProperty modePayProperty() { return modePay; }

    // État livraison
    public String getEtatLiv() { return etatLiv.get(); }
    public void setEtatLiv(String value) { etatLiv.set(value); }
    public StringProperty etatLivProperty() { return etatLiv; }
}
