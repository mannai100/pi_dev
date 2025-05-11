package entities;

import java.util.Date;

public class Avis {
    private int id;
    private int note; // Note de 1 à 5 étoiles
    private String commentaire;
    private Date dateAvis;
    private User user;
    private Event event;

    public Avis() {
        this.dateAvis = new Date();
    }

    public Avis(int id, int note, String commentaire, Date dateAvis, User user, Event event) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.user = user;
        this.event = event;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        if (note < 1) note = 1;
        if (note > 5) note = 5;
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(Date dateAvis) {
        this.dateAvis = dateAvis;
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

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateAvis=" + dateAvis +
                ", user=" + (user != null ? user.getId() : "null") +
                ", event=" + (event != null ? event.getId() : "null") +
                '}';
    }
}