package entities;

import java.util.Date;

/**
 * Classe représentant un événement
 */
public class Event {
    private int id;
    private User user;
    private String title;
    private String description;
    private Date date_debut;
    private Date date_fin;
    private String status;
    private String image;

    /**
     * Constructeur par défaut
     */
    public Event() {
    }

    /**
     * Constructeur avec tous les paramètres
     * @param id Identifiant de l'événement
     * @param title Titre de l'événement
     * @param description Description de l'événement
     * @param date_debut Date de début de l'événement
     * @param date_fin Date de fin de l'événement
     * @param status Statut de l'événement
     * @param image Image de l'événement
     */
    public Event(int id, User user, String title, String description, Date date_debut, Date date_fin, String status, String image) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        // Le champ max_participants a été supprimé
        this.status = status;
        this.image = image;
    }

    /**
     * Constructeur sans id (pour les nouveaux événements)
     * @param title Titre de l'événement
     * @param description Description de l'événement
     * @param date_debut Date de début de l'événement
     * @param date_fin Date de fin de l'événement
     * @param status Statut de l'événement
     * @param image Image de l'événement
     */
    public Event(User user, String title, String description, Date date_debut, Date date_fin, String status, String image) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        // Le champ max_participants a été supprimé
        this.status = status;
        this.image = image;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
    }

    // Les getters et setters pour max_participants ont été supprimés

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", user=" + user +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                // Le champ max_participants a été supprimé +
                ", status='" + status + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
