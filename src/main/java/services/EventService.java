package services;

import entities.Event;
import entities.User;
import utils.EventValidator;
import utils.MyDatabase;

import java.util.Map;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les événements
 */
public class EventService {
    private static EventService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private EventService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    /**
     * Ajouter un nouvel événement
     * @param event L'événement à ajouter
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si l'événement est invalide
     */
    public void addEvent(Event event) throws SQLException, IllegalArgumentException {
        // Valider l'événement
        Map<String, String> errors = EventValidator.validate(event);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(EventValidator.formatErrors(errors));
        }

        String query = "INSERT INTO event (user_id, title, description, date_debut, date_fin, status, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, event.getUser().getId());
            statement.setString(2, event.getTitle());
            statement.setString(3, event.getDescription());
            statement.setTimestamp(4, new Timestamp(event.getDate_debut().getTime()));
            statement.setTimestamp(5, new Timestamp(event.getDate_fin().getTime()));
            statement.setString(6, event.getStatus());
            statement.setString(7, event.getImage());

            statement.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Mettre à jour un événement existant
     * @param event L'événement à mettre à jour
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si l'événement est invalide
     */
    public void updateEvent(Event event) throws SQLException, IllegalArgumentException {
        // Valider l'événement
        Map<String, String> errors = EventValidator.validate(event);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(EventValidator.formatErrors(errors));
        }

        // Vérifier que l'événement existe
        if (event.getId() <= 0 || getEventById(event.getId()) == null) {
            throw new IllegalArgumentException("L'événement n'existe pas");
        }

        String query = "UPDATE event SET title = ?, description = ?, date_debut = ?, date_fin = ?, status = ?, image = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, event.getTitle());
            statement.setString(2, event.getDescription());
            statement.setTimestamp(3, new Timestamp(event.getDate_debut().getTime()));
            statement.setTimestamp(4, new Timestamp(event.getDate_fin().getTime()));
            statement.setString(5, event.getStatus());
            statement.setString(6, event.getImage());
            statement.setInt(7, event.getId());

            statement.executeUpdate();
        }
    }

    /**
     * Supprimer un événement
     * @param eventId L'ID de l'événement à supprimer
     * @throws SQLException En cas d'erreur SQL
     */
    public void deleteEvent(int eventId) throws SQLException {
        String query = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }
    }

    /**
     * Récupérer un événement par son ID
     * @param eventId L'ID de l'événement
     * @return L'événement ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public Event getEventById(int eventId) throws SQLException {
        String query = "SELECT e.*, u.id as user_id, u.nom, u.prenom, u.email FROM event e JOIN user u ON e.user_id = u.id WHERE e.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createEventFromResultSet(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * Récupérer tous les événements
     * @return La liste des événements
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.id as user_id, u.nom, u.prenom, u.email FROM event e JOIN user u ON e.user_id = u.id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                events.add(createEventFromResultSet(resultSet));
            }
        }

        return events;
    }

    /**
     * Récupérer les événements organisés par un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return La liste des événements
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Event> getEventsByUser(int userId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.id as user_id, u.nom, u.prenom, u.email FROM event e JOIN user u ON e.user_id = u.id WHERE e.user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(createEventFromResultSet(resultSet));
                }
            }
        }

        return events;
    }

    /**
     * Créer un objet Event à partir d'un ResultSet
     * @param resultSet Le ResultSet contenant les données de l'événement
     * @return L'objet Event créé
     * @throws SQLException En cas d'erreur SQL
     */
    private Event createEventFromResultSet(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getInt("id"));
        event.setTitle(resultSet.getString("title"));
        event.setDescription(resultSet.getString("description"));
        event.setDate_debut(resultSet.getTimestamp("date_debut"));
        event.setDate_fin(resultSet.getTimestamp("date_fin"));
        // Le champ max_participants a été supprimé
        event.setStatus(resultSet.getString("status"));
        event.setImage(resultSet.getString("image"));

        // Créer l'organisateur
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setNom(resultSet.getString("nom"));
        user.setPrenom(resultSet.getString("prenom"));
        user.setEmail(resultSet.getString("email"));

        event.setUser(user);

        return event;
    }
}
