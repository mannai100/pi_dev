package utils;

import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la validation des données
 */
public class ValidationUtils {

    // Regex pour valider un email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex pour valider un numéro de téléphone (format international)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{8,15}$"
    );

    /**
     * Vérifie si une chaîne est nulle ou vide
     * @param str La chaîne à vérifier
     * @return true si la chaîne est nulle ou vide, false sinon
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Vérifie si une chaîne a une longueur minimale
     * @param str La chaîne à vérifier
     * @param minLength La longueur minimale
     * @return true si la chaîne a une longueur minimale, false sinon
     */
    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    /**
     * Vérifie si une chaîne a une longueur maximale
     * @param str La chaîne à vérifier
     * @param maxLength La longueur maximale
     * @return true si la chaîne a une longueur maximale, false sinon
     */
    public static boolean hasMaxLength(String str, int maxLength) {
        return str != null && str.length() <= maxLength;
    }

    /**
     * Vérifie si une chaîne est un email valide
     * @param email L'email à vérifier
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Vérifie si une chaîne est un numéro de téléphone valide
     * @param phone Le numéro de téléphone à vérifier
     * @return true si le numéro de téléphone est valide, false sinon
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Vérifie si un nombre est positif
     * @param number Le nombre à vérifier
     * @return true si le nombre est positif, false sinon
     */
    public static boolean isPositive(int number) {
        return number > 0;
    }

    /**
     * Vérifie si un nombre est négatif
     * @param number Le nombre à vérifier
     * @return true si le nombre est négatif, false sinon
     */
    public static boolean isNegative(int number) {
        return number < 0;
    }

    /**
     * Vérifie si un nombre est dans une plage
     * @param number Le nombre à vérifier
     * @param min La valeur minimale
     * @param max La valeur maximale
     * @return true si le nombre est dans la plage, false sinon
     */
    public static boolean isInRange(int number, int min, int max) {
        return number >= min && number <= max;
    }

    /**
     * Nettoie une chaîne en supprimant les espaces en début et fin
     * @param str La chaîne à nettoyer
     * @return La chaîne nettoyée
     */
    public static String cleanString(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * Tronque une chaîne à une longueur maximale
     * @param str La chaîne à tronquer
     * @param maxLength La longueur maximale
     * @return La chaîne tronquée
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
