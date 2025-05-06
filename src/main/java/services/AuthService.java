package services;

import entities.User;
import entities.UserSession;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Service pour gérer l'authentification des utilisateurs
 */
public class AuthService {
    private static AuthService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private AuthService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authentifier un utilisateur avec son email et son mot de passe
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return L'utilisateur authentifié ou null si l'authentification échoue
     * @throws SQLException En cas d'erreur SQL
     */
    public User login(String email, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password); // Note: Dans une application réelle, il faudrait hacher le mot de passe

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Créer un objet User avec les données de la base de données
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setNom(resultSet.getString("nom"));
                    user.setPrenom(resultSet.getString("prenom"));
                    user.setEmail(resultSet.getString("email"));
                    user.setAdresse(resultSet.getString("adresse"));
                    user.setTelephone(resultSet.getString("telephone"));
                    user.setVerified(resultSet.getBoolean("is_verified"));
                    user.setCreated_at(resultSet.getTimestamp("created_at"));

                    // Stocker l'utilisateur dans la session
                    UserSession.getInstance().setCurrentUser(user);

                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Enregistrer un nouvel utilisateur
     * @param user L'utilisateur à enregistrer
     * @throws SQLException En cas d'erreur SQL
     */
    public void register(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, email, password, adresse, telephone, is_verified, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getAdresse());
            statement.setString(6, user.getTelephone());
            statement.setBoolean(7, user.isVerified());
            statement.setTimestamp(8, user.getCreated_at());

            statement.executeUpdate();
        }
    }

    /**
     * Vérifier si un email existe déjà dans la base de données
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Vérifier si un numéro de téléphone existe déjà dans la base de données
     * @param telephone Le numéro de téléphone à vérifier
     * @return true si le téléphone existe, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean telephoneExists(String telephone) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE telephone = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, telephone);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Déconnecter l'utilisateur actuel
     */
    public void logout() {
        UserSession.getInstance().logout();
    }

    /**
     * Obtenir l'utilisateur actuellement connecté
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
    }
}
