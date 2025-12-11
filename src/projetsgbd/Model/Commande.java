package projetsgbd.Model;

import java.sql.Date;
import javafx.beans.property.*;

public class Commande {
    
    private final IntegerProperty nocde;
    private final IntegerProperty noclt;
    private final StringProperty nomClient; // Pour affichage dans le tableau
    private final ObjectProperty<Date> dateCde;
    private final StringProperty etatCde;
    private final DoubleProperty montantTotal; // Calculé ou stocké

    // Constructeur par défaut
    public Commande() {
        this.nocde = new SimpleIntegerProperty(0);
        this.noclt = new SimpleIntegerProperty(0);
        this.nomClient = new SimpleStringProperty("");
        this.dateCde = new SimpleObjectProperty<>();
        this.etatCde = new SimpleStringProperty("");
        this.montantTotal = new SimpleDoubleProperty(0.0);
    }

    // Constructeur complet
    public Commande(int nocde, int noclt, String nomClient, Date dateCde, String etatCde, double montant) {
        this.nocde = new SimpleIntegerProperty(nocde);
        this.noclt = new SimpleIntegerProperty(noclt);
        this.nomClient = new SimpleStringProperty(nomClient);
        this.dateCde = new SimpleObjectProperty<>(dateCde);
        this.etatCde = new SimpleStringProperty(etatCde);
        this.montantTotal = new SimpleDoubleProperty(montant);
    }

    // ================= GETTERS & SETTERS PROPERTYS =================

    public IntegerProperty nocdeProperty() { return nocde; }
    public int getNocde() { return nocde.get(); }
    public void setNocde(int v) { nocde.set(v); }

    public IntegerProperty nocltProperty() { return noclt; }
    public int getNoclt() { return noclt.get(); }
    public void setNoclt(int v) { noclt.set(v); }

    public StringProperty nomClientProperty() { return nomClient; }
    public String getNomClient() { return nomClient.get(); }
    public void setNomClient(String v) { nomClient.set(v); }

    public ObjectProperty<Date> dateCdeProperty() { return dateCde; }
    public Date getDateCde() { return dateCde.get(); }
    public void setDateCde(Date v) { dateCde.set(v); }

    public StringProperty etatCdeProperty() { return etatCde; }
    public String getEtatCde() { return etatCde.get(); }
    public void setEtatCde(String v) { etatCde.set(v); }
    
    public DoubleProperty montantTotalProperty() { return montantTotal; }
    public double getMontantTotal() { return montantTotal.get(); }
    public void setMontantTotal(double v) { montantTotal.set(v); }
}
