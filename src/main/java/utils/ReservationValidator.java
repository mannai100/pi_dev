package utils;

import entities.ReserverEvent;
import entities.Event;
import entities.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitaire pour valider les données d'une réservation d'événement
 */
public class ReservationValidator {

    /**
     * Valide les données d'une réservation d'événement
     * @param reservation La réservation à valider
     * @return Une map contenant les erreurs de validation (vide si aucune erreur)
     */
    public static Map<String, String> validate(ReserverEvent reservation) {
        Map<String, String> errors = new HashMap<>();

        // Validation de l'utilisateur
        if (reservation.getUser() == null) {
            errors.put("user", "L'utilisateur est obligatoire");
        } else if (reservation.getUser().getId() <= 0) {
            errors.put("user", "L'ID de l'utilisateur est invalide");
        }

        // Validation de l'événement
        if (reservation.getEvent() == null) {
            errors.put("event", "L'événement est obligatoire");
        } else if (reservation.getEvent().getId() <= 0) {
            errors.put("event", "L'ID de l'événement est invalide");
        } else {
            // Vérifier si l'événement est disponible
            Event event = reservation.getEvent();
            Date now = new Date();
            
            if (event.getDate_debut().before(now)) {
                errors.put("event", "L'événement a déjà commencé");
            }
            
            if (event.getStatus() != null && !event.getStatus().equals("actif")) {
                errors.put("event", "L'événement n'est pas disponible pour réservation (statut : " + event.getStatus() + ")");
            }
        }

        // Validation de la date de réservation
        if (reservation.getDateReservation() == null) {
            errors.put("dateReservation", "La date de réservation est obligatoire");
        }

        // Validation du statut
        if (reservation.getStatut() == null || reservation.getStatut().trim().isEmpty()) {
            errors.put("statut", "Le statut est obligatoire");
        } else if (!isValidStatus(reservation.getStatut())) {
            errors.put("statut", "Le statut est invalide (valeurs acceptées : en attente, confirmé, annulé)");
        }

        return errors;
    }

    /**
     * Vérifie si le statut est valide
     * @param statut Le statut à vérifier
     * @return true si le statut est valide, false sinon
     */
    private static boolean isValidStatus(String statut) {
        return statut.equals("en attente") || statut.equals("confirmé") || statut.equals("annulé");
    }

    /**
     * Vérifie si une réservation est valide
     * @param reservation La réservation à vérifier
     * @return true si la réservation est valide, false sinon
     */
    public static boolean isValid(ReserverEvent reservation) {
        return validate(reservation).isEmpty();
    }

    /**
     * Vérifie si un utilisateur peut réserver un événement
     * @param user L'utilisateur
     * @param event L'événement
     * @return true si l'utilisateur peut réserver l'événement, false sinon
     */
    public static boolean canReserve(User user, Event event) {
        // Vérifier si l'utilisateur est valide
        if (user == null || user.getId() <= 0) {
            return false;
        }

        // Vérifier si l'événement est valide
        if (event == null || event.getId() <= 0) {
            return false;
        }

        // Vérifier si l'événement est disponible
        Date now = new Date();
        if (event.getDate_debut().before(now)) {
            return false;
        }

        // Vérifier si l'événement est actif
        if (event.getStatus() == null || !event.getStatus().equals("actif")) {
            return false;
        }

        // Vérifier si l'utilisateur est l'utilisateur de l'événement
        if (event.getUser() != null && event.getUser().getId() == user.getId()) {
            return false; // L'utilisateur ne peut pas réserver son propre événement
        }

        return true;
    }

    /**
     * Formate les erreurs de validation en une chaîne de caractères
     * @param errors Les erreurs de validation
     * @return Une chaîne de caractères contenant les erreurs de validation
     */
    public static String formatErrors(Map<String, String> errors) {
        if (errors.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Erreurs de validation :\n");
        
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            sb.append("- ").append(entry.getValue()).append("\n");
        }
        
        return sb.toString();
    }
}
