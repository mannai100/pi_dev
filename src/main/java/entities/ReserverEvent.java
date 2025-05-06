package entities;

import java.util.Date;

/**
 * Classe représentant une réservation d'événement
 * Fait la jointure entre un utilisateur et un événement
 */
public class ReserverEvent {
    private int id;
    private User user;
    private Event event;
    private Date dateReservation;
    private String statut; // Par exemple: "confirmé", "en attente", "annulé"

    /**
     * Constructeur par défaut
     */
    public ReserverEvent() {
    }

    /**
     * Constructeur avec tous les paramètres
     * @param id Identifiant de la réservation
     * @param user Utilisateur qui réserve
     * @param event Événement réservé
     * @param dateReservation Date de la réservation
     * @param statut Statut de la réservation
     */
    public ReserverEvent(int id, User user, Event event, Date dateReservation, String statut) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.dateReservation = dateReservation;
        this.statut = statut;
    }

    /**
     * Constructeur sans id (pour les nouvelles réservations)
     * @param user Utilisateur qui réserve
     * @param event Événement réservé
     * @param dateReservation Date de la réservation
     * @param statut Statut de la réservation
     */
    public ReserverEvent(User user, Event event, Date dateReservation, String statut) {
        this.user = user;
        this.event = event;
        this.dateReservation = dateReservation;
        this.statut = statut;
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "ReserverEvent{" +
                "id=" + id +
                ", user=" + user +
                ", event=" + event +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                '}';
    }
}
