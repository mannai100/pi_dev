package services;

import entities.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux utilisateurs
 */
public class UserService {
    private static UserService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Récupérer un utilisateur par son ID
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        
        return null;
    }

    /**
     * Récupérer un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        
        return null;
    }

    /**
     * Mettre à jour les informations d'un utilisateur
     * @param user L'utilisateur à mettre à jour
     * @throws SQLException En cas d'erreur SQL
     */
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, adresse = ?, telephone = ?, imageUrl = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getAdresse());
            statement.setString(5, user.getTelephone());
            statement.setString(6, user.getImageUrl());
            statement.setInt(7, user.getId());
            
            statement.executeUpdate();
        }
    }

    /**
     * Mettre à jour le mot de passe d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe
     * @throws SQLException En cas d'erreur SQL
     */
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            
            statement.executeUpdate();
        }
    }

    /**
     * Vérifier si un email existe déjà (pour un autre utilisateur)
     * @param email L'email à vérifier
     * @param userId L'ID de l'utilisateur actuel (pour exclure de la vérification)
     * @return true si l'email existe pour un autre utilisateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean emailExistsForOtherUser(String email, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ? AND id != ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setInt(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Créer un objet User à partir d'un ResultSet
     * @param resultSet Le ResultSet contenant les données de l'utilisateur
     * @return L'objet User créé
     * @throws SQLException En cas d'erreur SQL
     */
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setNom(resultSet.getString("nom"));
        user.setPrenom(resultSet.getString("prenom"));
        user.setEmail(resultSet.getString("email"));
        user.setAdresse(resultSet.getString("adresse"));
        user.setTelephone(resultSet.getString("telephone"));
        user.setVerified(resultSet.getBoolean("is_verified"));
        user.setCreated_at(resultSet.getTimestamp("created_at"));
        user.setPassword(resultSet.getString("password"));
        user.setImageUrl(resultSet.getString("imageUrl"));
        
        // Récupérer les rôles
        String roleString = resultSet.getString("role");
        if (roleString != null && !roleString.isEmpty()) {
            List<String> roles = new ArrayList<>();
            for (String role : roleString.split(",")) {
                roles.add(role.trim());
            }
            user.setRole(roles);
        }
        
        return user;
    }
}
