package services;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HolidayService {

    // la clé API que pour Calendarific
    private static final String API_KEY = "F2odPDmQ6t9QGSPMMTi50RUSXRbpkJPb";
    private static final String API_URL = "https://calendarific.com/api/v2/holidays";

    // Instance unique pour le pattern Singleton
    private static HolidayService instance;

    // Constructeur privé pour empêcher l'instanciation directe
    private HolidayService() {
        // Constructeur privé
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static HolidayService getInstance() {
        if (instance == null) {
            instance = new HolidayService();
        }
        return instance;
    }

    public List<String> getHolidays() throws IOException {
        String urlString = API_URL + "?api_key=" + API_KEY + "&country=TN&year=2025";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        List<Holiday> holidays = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("API request failed with response code: " + connection.getResponseCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Afficher la réponse brute pour le débogage
            System.out.println("Réponse API brute: " + response.toString());

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray holidaysArray = jsonResponse.getJSONObject("response").getJSONArray("holidays");

            System.out.println("\nTraitement des jours fériés:");
            for (int i = 0; i < holidaysArray.length(); i++) {
                JSONObject holiday = holidaysArray.getJSONObject(i);
                String holidayName = holiday.getString("name");

                // Afficher le nom original et sa traduction
                System.out.println("Nom original: " + holidayName);



                JSONObject dateObj = holiday.getJSONObject("date");
                int year = dateObj.getJSONObject("datetime").getInt("year");
                int month = dateObj.getJSONObject("datetime").getInt("month");
                int day = dateObj.getJSONObject("datetime").getInt("day");

                LocalDate holidayDate = LocalDate.of(year, month, day);
                holidays.add(new Holiday(holidayName, holidayDate));
            }
        } catch (JSONException e) {
            System.err.println("Erreur lors de l'analyse des données: " + e.getMessage());
            throw new IOException("Échec de l'analyse des données", e);
        }

        // Formater et trier les résultats
        List<String> formattedHolidays = holidays.stream()
                .filter(holiday -> !holiday.date.isBefore(today))
                .sorted((h1, h2) -> h1.date.compareTo(h2.date))
                .map(this::formatHoliday)
                .collect(Collectors.toList());

        // Afficher les résultats finaux
        System.out.println("\nJours fériés formatés:");
        formattedHolidays.forEach(System.out::println);

        return formattedHolidays;
    }

    private String formatHoliday(Holiday holiday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
        return holiday.name + " : " + holiday.date.format(formatter);
    }

    private static class Holiday {
        String name;
        LocalDate date;

        Holiday(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }
    }
}
