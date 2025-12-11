package projetsgbd.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Utilisateur {

    private final IntegerProperty idpers = new SimpleIntegerProperty();
    private final StringProperty login = new SimpleStringProperty();
    private final StringProperty motDePasse = new SimpleStringProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty telephone = new SimpleStringProperty();
    private final StringProperty adresse = new SimpleStringProperty();
    private final StringProperty ville = new SimpleStringProperty();
    private final StringProperty poste = new SimpleStringProperty();
    private final StringProperty dateEmbauche = new SimpleStringProperty();

    public Utilisateur(String login, String motDePasse, String nom, String prenom,
                       String telephone, String adresse, String ville, String poste) {
        this.login.set(login);
        this.motDePasse.set(motDePasse);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.telephone.set(telephone);
        this.adresse.set(adresse);
        this.ville.set(ville);
        this.poste.set(poste);
    }

public Utilisateur(String login, String motDePasse, String poste) {
        this.login.set(login);
        this.motDePasse.set(motDePasse);
          this.poste.set(poste);
        
    }

    public int getIdpers() { return idpers.get(); }
    public void setIdpers(int id) { this.idpers.set(id); }

    public String getLogin() { return login.get(); }
    public void setLogin(String l) { this.login.set(l); }

    public String getMotDePasse() { return motDePasse.get(); }
    public void setMotDePasse(String m) { this.motDePasse.set(m); }

    public String getNom() { return nom.get(); }
    public void setNom(String n) { this.nom.set(n); }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String p) { this.prenom.set(p); }

    public String getNomPrenom() { return getNom() + " " + getPrenom(); }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String t) { this.telephone.set(t); }

    public String getAdresse() { return adresse.get(); }
    public void setAdresse(String a) { this.adresse.set(a); }

    public String getVille() { return ville.get(); }
    public void setVille(String v) { this.ville.set(v); }

    public String getPoste() { return poste.get(); }
    public void setPoste(String p) { this.poste.set(p); }

    public String getDateEmbauche() { return dateEmbauche.get(); }
    public void setDateEmbauche(String d) { this.dateEmbauche.set(d); }

  
    public StringProperty loginProperty() { return login; }
    public StringProperty nomPrenomProperty() { return new SimpleStringProperty(getNomPrenom()); }
    public StringProperty posteProperty() { return poste; }
    public StringProperty telephoneProperty() { return telephone; }
    public StringProperty dateEmbaucheProperty() { return dateEmbauche; }
}
