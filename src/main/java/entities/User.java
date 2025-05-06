package entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String nom;
    private String email;
    private String prenom;
    private String adresse;
    private String telephone;
    private boolean isVerified;
    private Timestamp created_at;
    private List<String> role;
    private String password;
    private String imageUrl;

    // Constructeur par défaut
    public User() {
        this.role = new ArrayList<>();
    }

    // Constructeur avec tous les paramètres
    public User(int id, String nom, String email, String prenom, String adresse, String telephone, boolean isVerified, Timestamp created_at, String password, String imageUrl) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.isVerified = isVerified;
        this.created_at = created_at;
        this.password = password;
        this.imageUrl = imageUrl;
        this.role = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", isVerified=" + isVerified +
                ", created_at=" + created_at +
                ", role=" + role +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
