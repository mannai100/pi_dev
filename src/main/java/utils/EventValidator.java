package utils;

import entities.Event;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EventValidator {

    // Regex pour valider le titre (lettres, chiffres, espaces et ponctuations de base)
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,;:!?'\"()-]+$");

    // Regex pour valider l'URL d'une image
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("^(https?|ftp)://.*\\.(jpeg|jpg|png|gif|bmp|webp)$", Pattern.CASE_INSENSITIVE);

    /**
     * Valide le titre d'un événement
     * @param title Le titre à valider
     * @return Un message d'erreur ou null si le titre est valide
     */
    public static String isValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Le titre est obligatoire";
        }
        if (title.length() < 3) {
            return "Le titre doit contenir au moins 3 caractères";
        }
        if (title.length() > 255) {
            return "Le titre ne doit pas dépasser 255 caractères";
        }
        if (!TITLE_PATTERN.matcher(title).matches()) {
            return "Le titre contient des caractères non autorisés";
        }
        return null;
    }

    /**
     * Valide la description d'un événement
     * @param description La description à valider
     * @return Un message d'erreur ou null si la description est valide
     */
    public static String isValidDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "La description est obligatoire";
        }
        if (description.length() < 10) {
            return "La description doit contenir au moins 10 caractères";
        }
        if (description.length() > 2000) {
            return "La description ne doit pas dépasser 2000 caractères";
        }
        return null;
    }

    /**
     * Valide la date de début d'un événement
     * @param dateDebut La date de début à valider
     * @return Un message d'erreur ou null si la date est valide
     */
    public static String isValidDateDebut(Date dateDebut) {
        if (dateDebut == null) {
            return "La date de début est obligatoire";
        }

        // Vérifier que la date est aujourd'hui ou dans le futur
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        if (dateDebut.before(today)) {
            return "La date de début doit être aujourd'hui ou dans le futur";
        }

        return null;
    }

    /**
     * Valide la date de fin d'un événement
     * @param dateFin La date de fin à valider
     * @param dateDebut La date de début pour comparaison
     * @return Un message d'erreur ou null si la date est valide
     */
    public static String isValidDateFin(Date dateFin, Date dateDebut) {
        if (dateFin == null) {
            return "La date de fin est obligatoire";
        }

        if (dateDebut != null && dateFin.before(dateDebut)) {
            return "La date de fin doit être après la date de début";
        }

        // Vérifier qu'il y a au moins une heure de différence entre la date de début et la date de fin
        if (dateDebut != null) {
            // Calculer la différence en millisecondes
            long differenceMillis = dateFin.getTime() - dateDebut.getTime();
            // Convertir en heures
            long differenceHeures = differenceMillis / (60 * 60 * 1000);

            if (differenceHeures < 1) {
                return "Il doit y avoir au moins une heure de différence entre la date de début et la date de fin";
            }
        }

        return null;
    }

    /**
     * Vérifie si deux dates sont le même jour
     * @param date1 Première date
     * @param date2 Deuxième date
     * @return true si les dates sont le même jour, false sinon
     */
    private static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    // La validation du nombre maximum de participants a été supprimée

    /**
     * Valide le statut d'un événement
     * @param status Le statut à valider
     * @return Un message d'erreur ou null si le statut est valide
     */
    public static String isValidStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Le statut est obligatoire";
        }

        if (!status.equals("actif") && !status.equals("annulé") && !status.equals("complet")) {
            return "Le statut est invalide (valeurs acceptées : actif, annulé, complet)";
        }

        return null;
    }

    /**
     * Valide le chemin de l'image d'un événement
     * @param imagePath Le chemin de l'image à valider
     * @return Un message d'erreur ou null si le chemin est valide
     */
    public static String isValidImagePath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return "L'image est obligatoire";
        }

        // Vérifier si c'est une URL externe ou un chemin local
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // C'est une URL externe, vérifier qu'elle est valide
            if (!IMAGE_URL_PATTERN.matcher(imagePath).matches()) {
                return "L'URL de l'image n'est pas valide. Elle doit se terminer par .jpg, .jpeg, .png, .gif, .bmp ou .webp";
            }
        } else if (!imagePath.startsWith("/images/events/")) {
            // C'est un chemin local, vérifier qu'il est dans le bon dossier
            return "Le chemin de l'image n'est pas valide. Il doit être dans le dossier /images/events/";
        }

        return null;
    }

    /**
     * Valide les données d'un événement
     * @param event L'événement à valider
     * @return Une map contenant les erreurs de validation (vide si aucune erreur)
     */
    public static Map<String, String> validate(Event event) {
        Map<String, String> errors = new HashMap<>();

        // Validation du titre
        String titleError = isValidTitle(event.getTitle());
        if (titleError != null) {
            errors.put("title", titleError);
        }

        // Validation de la description
        String descriptionError = isValidDescription(event.getDescription());
        if (descriptionError != null) {
            errors.put("description", descriptionError);
        }

        // Validation de la date de début
        String dateDebutError = isValidDateDebut(event.getDate_debut());
        if (dateDebutError != null) {
            errors.put("date_debut", dateDebutError);
        }

        // Validation de la date de fin
        String dateFinError = isValidDateFin(event.getDate_fin(), event.getDate_debut());
        if (dateFinError != null) {
            errors.put("date_fin", dateFinError);
        }

        // La validation du nombre maximum de participants a été supprimée

        // Validation du statut
        String statusError = isValidStatus(event.getStatus());
        if (statusError != null) {
            errors.put("status", statusError);
        }

        // Validation de l'image
        String imageError = isValidImagePath(event.getImage());
        if (imageError != null) {
            errors.put("image", imageError);
        }

        return errors;
    }

    /**
     * Vérifie si un événement est valide
     * @param event L'événement à vérifier
     * @return true si l'événement est valide, false sinon
     */
    public static boolean isValid(Event event) {
        return validate(event).isEmpty();
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

        for (Map.Entry<String, String> entry : errors.entrySet()) {
            sb.append("- ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }
}
