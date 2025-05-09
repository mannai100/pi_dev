package services;

import entities.Avis;
import entities.Event;
import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class AvisService {
    private static AvisService instance;
    private Connection connection;
    private AuthService authService;
    private EventService eventService;

    private AvisService() {
        connection = MyDatabase.getInstance().getConnection();
        authService = AuthService.getInstance();
        eventService = EventService.getInstance();
    }

    public static AvisService getInstance() {
        if (instance == null) {
            instance = new AvisService();
        }
        return instance;
    }

    /**
     * Ajouter un nouvel avis
     * @param avis L'avis à ajouter
     * @return true si l'avis a été ajouté avec succès, false sinon
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si l'utilisateur a déjà donné un avis ou est l'organisateur de l'événement
     */
    public boolean addAvis(Avis avis) throws SQLException, IllegalArgumentException {
        // Vérifier si l'utilisateur a déjà donné un avis pour cet événement
        if (hasUserRatedEvent(avis.getUser().getId(), avis.getEvent().getId())) {
            throw new IllegalArgumentException("Vous avez déjà donné un avis pour cet événement");
        }
        
        // Vérifier si l'utilisateur est l'organisateur de l'événement
        if (avis.getEvent().getUser() != null && avis.getEvent().getUser().getId() == avis.getUser().getId()) {
            throw new IllegalArgumentException("Vous ne pouvez pas évaluer votre propre événement");
        }

        String query = "INSERT INTO avis (note, commentaire, date_avis, user_id, event_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, avis.getNote());
            statement.setString(2, avis.getCommentaire());
            statement.setTimestamp(3, new Timestamp(avis.getDateAvis().getTime()));
            statement.setInt(4, avis.getUser().getId());
            statement.setInt(5, avis.getEvent().getId());

            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        avis.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Vérifier si un utilisateur a déjà donné un avis pour un événement
     * @param userId L'ID de l'utilisateur
     * @param eventId L'ID de l'événement
     * @return true si l'utilisateur a déjà donné un avis, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean hasUserRatedEvent(int userId, int eventId) throws SQLException {
        String query = "SELECT COUNT(*) FROM avis WHERE user_id = ? AND event_id = ?";
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
     * Récupérer tous les avis pour un événement
     * @param eventId L'ID de l'événement
     * @return La liste des avis pour l'événement
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Avis> getAvisByEvent(int eventId) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT a.*, u.id as user_id, u.nom, u.prenom, u.email FROM avis a " +
                       "JOIN user u ON a.user_id = u.id " +
                       "WHERE a.event_id = ? ORDER BY a.date_avis DESC";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, eventId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Avis avis = new Avis();
                    avis.setId(resultSet.getInt("id"));
                    avis.setNote(resultSet.getInt("note"));
                    avis.setCommentaire(resultSet.getString("commentaire"));
                    avis.setDateAvis(resultSet.getTimestamp("date_avis"));
                    
                    // Créer l'utilisateur à partir des données de la requête
                    User user = new User();
                    user.setId(resultSet.getInt("user_id"));
                    user.setNom(resultSet.getString("nom"));
                    user.setPrenom(resultSet.getString("prenom"));
                    user.setEmail(resultSet.getString("email"));
                    avis.setUser(user);
                    
                    // Récupérer l'événement
                    Event event = eventService.getEventById(eventId);
                    avis.setEvent(event);
                    
                    avisList.add(avis);
                }
            }
        }
        
        return avisList;
    }

    /**
     * Calculer la note moyenne d'un événement
     * @param eventId L'ID de l'événement
     * @return La note moyenne de l'événement (0 si aucun avis)
     * @throws SQLException En cas d'erreur SQL
     */
    public double getAverageRating(int eventId) throws SQLException {
        String query = "SELECT AVG(note) as moyenne FROM avis WHERE event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, eventId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double moyenne = resultSet.getDouble("moyenne");
                    return resultSet.wasNull() ? 0 : moyenne;
                }
            }
        }
        return 0;
    }

    /**
     * Supprimer un avis
     * @param avisId L'ID de l'avis à supprimer
     * @return true si l'avis a été supprimé avec succès, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean deleteAvis(int avisId) throws SQLException {
        String query = "DELETE FROM avis WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, avisId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Mettre à jour un avis
     * @param avis L'avis à mettre à jour
     * @return true si l'avis a été mis à jour avec succès, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean updateAvis(Avis avis) throws SQLException {
        String query = "UPDATE avis SET note = ?, commentaire = ?, date_avis = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, avis.getNote());
            statement.setString(2, avis.getCommentaire());
            statement.setTimestamp(3, new Timestamp(new Date().getTime())); // Mettre à jour la date
            statement.setInt(4, avis.getId());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }
}