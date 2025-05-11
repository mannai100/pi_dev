package utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Programme de test pour vérifier la validation des dates dans EventValidator
 */
public class EventValidatorTest {

    public static void main(String[] args) {
        // Test 1: Dates le même jour, moins d'une heure de différence
        System.out.println("Test 1: Dates le même jour, moins d'une heure de différence");
        Date dateDebut1 = createDate(2023, Calendar.DECEMBER, 15, 8, 0);
        Date dateFin1 = createDate(2023, Calendar.DECEMBER, 15, 8, 30);
        String result1 = EventValidator.isValidDateFin(dateFin1, dateDebut1);
        System.out.println("Résultat: " + (result1 != null ? result1 : "Validation réussie"));
        
        // Test 2: Dates le même jour, exactement une heure de différence
        System.out.println("\nTest 2: Dates le même jour, exactement une heure de différence");
        Date dateDebut2 = createDate(2023, Calendar.DECEMBER, 15, 8, 0);
        Date dateFin2 = createDate(2023, Calendar.DECEMBER, 15, 9, 0);
        String result2 = EventValidator.isValidDateFin(dateFin2, dateDebut2);
        System.out.println("Résultat: " + (result2 != null ? result2 : "Validation réussie"));
        
        // Test 3: Dates le même jour, plus d'une heure de différence
        System.out.println("\nTest 3: Dates le même jour, plus d'une heure de différence");
        Date dateDebut3 = createDate(2023, Calendar.DECEMBER, 15, 8, 0);
        Date dateFin3 = createDate(2023, Calendar.DECEMBER, 15, 10, 0);
        String result3 = EventValidator.isValidDateFin(dateFin3, dateDebut3);
        System.out.println("Résultat: " + (result3 != null ? result3 : "Validation réussie"));
        
        // Test 4: Dates différentes, plus d'une heure de différence
        System.out.println("\nTest 4: Dates différentes, plus d'une heure de différence");
        Date dateDebut4 = createDate(2023, Calendar.DECEMBER, 15, 8, 0);
        Date dateFin4 = createDate(2023, Calendar.DECEMBER, 16, 8, 0);
        String result4 = EventValidator.isValidDateFin(dateFin4, dateDebut4);
        System.out.println("Résultat: " + (result4 != null ? result4 : "Validation réussie"));
        
        // Test 5: Date de fin avant date de début
        System.out.println("\nTest 5: Date de fin avant date de début");
        Date dateDebut5 = createDate(2023, Calendar.DECEMBER, 15, 8, 0);
        Date dateFin5 = createDate(2023, Calendar.DECEMBER, 14, 8, 0);
        String result5 = EventValidator.isValidDateFin(dateFin5, dateDebut5);
        System.out.println("Résultat: " + (result5 != null ? result5 : "Validation réussie"));
    }
    
    /**
     * Crée une date avec les paramètres spécifiés
     */
    private static Date createDate(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
