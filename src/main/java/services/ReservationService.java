package services;

import entities.Event;
import entities.ReserverEvent;
import entities.User;
import utils.MyDatabase;
import utils.ReservationValidator;

import java.util.Map;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les réservations d'événements
 */
public class ReservationService {
    private static ReservationService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private ReservationService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    /**
     * Ajouter une nouvelle réservation
     * @param reservation La réservation à ajouter
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si la réservation est invalide
     */
    public void addReservation(ReserverEvent reservation) throws SQLException, IllegalArgumentException {
        // Valider la réservation
        Map<String, String> errors = ReservationValidator.validate(reservation);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(ReservationValidator.formatErrors(errors));
        }

        // Vérifier si l'utilisateur a déjà réservé cet événement
        if (hasUserReservedEvent(reservation.getUser().getId(), reservation.getEvent().getId())) {
            throw new IllegalArgumentException("Vous avez déjà réservé cet événement");
        }

        // Vérifier si l'utilisateur peut réserver cet événement
        if (!ReservationValidator.canReserve(reservation.getUser(), reservation.getEvent())) {
            throw new IllegalArgumentException("Vous ne pouvez pas réserver cet événement");
        }

        String query = "INSERT INTO reserver_event (user_id, event_id, date_reservation, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reservation.getUser().getId());
            statement.setInt(2, reservation.getEvent().getId());
            statement.setTimestamp(3, new Timestamp(reservation.getDateReservation().getTime()));
            statement.setString(4, reservation.getStatut());

            statement.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Mettre à jour le statut d'une réservation
     * @param reservationId L'ID de la réservation
     * @param statut Le nouveau statut
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si le statut est invalide ou si la réservation n'existe pas
     */
    public void updateReservationStatus(int reservationId, String statut) throws SQLException, IllegalArgumentException {
        // Vérifier que la réservation existe
        ReserverEvent reservation = getReservationById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("La réservation n'existe pas");
        }

        // Vérifier que le statut est valide
        if (statut == null || statut.trim().isEmpty() ||
                (!statut.equals("en attente") && !statut.equals("confirmé") && !statut.equals("annulé"))) {
            throw new IllegalArgumentException("Le statut est invalide (valeurs acceptées : en attente, confirmé, annulé)");
        }

        String query = "UPDATE reserver_event SET statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, statut);
            statement.setInt(2, reservationId);

            statement.executeUpdate();
        }
    }

    /**
     * Annuler une réservation
     * @param reservationId L'ID de la réservation
     * @throws SQLException En cas d'erreur SQL
     */
    public void cancelReservation(int reservationId) throws SQLException {
        updateReservationStatus(reservationId, "annulé");
    }

    /**
     * Récupérer une réservation par son ID
     * @param reservationId L'ID de la réservation
     * @return La réservation ou null si elle n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public ReserverEvent getReservationById(int reservationId) throws SQLException {
        String query = "SELECT r.*, " +
                "u.id as user_id, u.nom as user_nom, u.prenom as user_prenom, u.email as user_email, " +
                "e.id as event_id, e.title, e.description, e.date_debut, e.date_fin, e.max_participants, e.status, e.image " +
                "FROM reserver_event r " +
                "JOIN user u ON r.user_id = u.id " +
                "JOIN event e ON r.event_id = e.id " +
                "WHERE r.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createReservationFromResultSet(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * Récupérer toutes les réservations d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return La liste des réservations
     * @throws SQLException En cas d'erreur SQL
     */
    public List<ReserverEvent> getReservationsByUser(int userId) throws SQLException {
        List<ReserverEvent> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "u.id as user_id, u.nom as user_nom, u.prenom as user_prenom, u.email as user_email, " +
                "e.id as event_id, e.title, e.description, e.date_debut, e.date_fin, e.max_participants, e.status, e.image " +
                "FROM reserver_event r " +
                "JOIN user u ON r.user_id = u.id " +
                "JOIN event e ON r.event_id = e.id " +
                "WHERE r.user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(createReservationFromResultSet(resultSet));
                }
            }
        }

        return reservations;
    }

    /**
     * Récupérer toutes les réservations pour un événement
     * @param eventId L'ID de l'événement
     * @return La liste des réservations
     * @throws SQLException En cas d'erreur SQL
     */
    public List<ReserverEvent> getReservationsByEvent(int eventId) throws SQLException {
        List<ReserverEvent> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "u.id as user_id, u.nom as user_nom, u.prenom as user_prenom, u.email as user_email, " +
                "e.id as event_id, e.title, e.description, e.date_debut, e.date_fin, e.max_participants, e.status, e.image " +
                "FROM reserver_event r " +
                "JOIN user u ON r.user_id = u.id " +
                "JOIN event e ON r.event_id = e.id " +
                "WHERE r.event_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(createReservationFromResultSet(resultSet));
                }
            }
        }

        return reservations;
    }

    /**
     * Récupérer toutes les réservations
     * @return La liste de toutes les réservations
     * @throws SQLException En cas d'erreur SQL
     */
    public List<ReserverEvent> getAllReservations() throws SQLException {
        List<ReserverEvent> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "u.id as user_id, u.nom as user_nom, u.prenom as user_prenom, u.email as user_email, " +
                "e.id as event_id, e.title, e.description, e.date_debut, e.date_fin, e.max_participants, e.status, e.image " +
                "FROM reserver_event r " +
                "JOIN user u ON r.user_id = u.id " +
                "JOIN event e ON r.event_id = e.id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                reservations.add(createReservationFromResultSet(resultSet));
            }
        }

        return reservations;
    }

    /**
     * Vérifier si un utilisateur a déjà réservé un événement
     * @param userId L'ID de l'utilisateur
     * @param eventId L'ID de l'événement
     * @return true si l'utilisateur a déjà réservé l'événement, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean hasUserReservedEvent(int userId, int eventId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reserver_event WHERE user_id = ? AND event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Créer un objet ReserverEvent à partir d'un ResultSet
     * @param resultSet Le ResultSet contenant les données de la réservation
     * @return L'objet ReserverEvent créé
     * @throws SQLException En cas d'erreur SQL
     */
    private ReserverEvent createReservationFromResultSet(ResultSet resultSet) throws SQLException {
        ReserverEvent reservation = new ReserverEvent();
        reservation.setId(resultSet.getInt("id"));
        reservation.setDateReservation(resultSet.getTimestamp("date_reservation"));
        reservation.setStatut(resultSet.getString("statut"));

        // Créer l'utilisateur
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setNom(resultSet.getString("user_nom"));
        user.setPrenom(resultSet.getString("user_prenom"));
        user.setEmail(resultSet.getString("user_email"));

        reservation.setUser(user);

        // Créer l'événement
        Event event = new Event();
        event.setId(resultSet.getInt("event_id"));
        event.setTitle(resultSet.getString("title"));
        event.setDescription(resultSet.getString("description"));
        event.setDate_debut(resultSet.getTimestamp("date_debut"));
        event.setDate_fin(resultSet.getTimestamp("date_fin"));
        event.setMax_participants(resultSet.getInt("max_participants"));
        event.setStatus(resultSet.getString("status"));
        event.setImage(resultSet.getString("image"));

        reservation.setEvent(event);

        return reservation;
    }
}
