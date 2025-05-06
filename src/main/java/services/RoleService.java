package services;

import entities.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour gérer les rôles des utilisateurs
 */
public class RoleService {
    private static RoleService instance;
    private final Connection connection;

    // Constantes pour les rôles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private RoleService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }

    /**
     * Récupérer les rôles d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return La liste des rôles de l'utilisateur
     * @throws SQLException En cas d'erreur SQL
     */
    public List<String> getUserRoles(int userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT role FROM user WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String roleString = resultSet.getString("role");
                    if (roleString != null && !roleString.isEmpty()) {
                        // Diviser la chaîne de rôles en liste
                        roles = Arrays.asList(roleString.split(","));
                        // Nettoyer les espaces éventuels
                        for (int i = 0; i < roles.size(); i++) {
                            roles.set(i, roles.get(i).trim());
                        }
                    }
                }
            }
        }
        
        return roles;
    }

    /**
     * Ajouter un rôle à un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param role Le rôle à ajouter
     * @throws SQLException En cas d'erreur SQL
     */
    public void addRoleToUser(int userId, String role) throws SQLException {
        // Récupérer les rôles actuels
        List<String> currentRoles = getUserRoles(userId);
        
        // Vérifier si le rôle existe déjà
        if (currentRoles.contains(role)) {
            return;
        }
        
        // Ajouter le nouveau rôle
        List<String> newRoles = new ArrayList<>(currentRoles);
        newRoles.add(role);
        
        // Mettre à jour les rôles dans la base de données
        updateUserRoles(userId, newRoles);
    }

    /**
     * Supprimer un rôle d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param role Le rôle à supprimer
     * @throws SQLException En cas d'erreur SQL
     */
    public void removeRoleFromUser(int userId, String role) throws SQLException {
        // Récupérer les rôles actuels
        List<String> currentRoles = getUserRoles(userId);
        
        // Vérifier si le rôle existe
        if (!currentRoles.contains(role)) {
            return;
        }
        
        // Supprimer le rôle
        List<String> newRoles = new ArrayList<>(currentRoles);
        newRoles.remove(role);
        
        // Mettre à jour les rôles dans la base de données
        updateUserRoles(userId, newRoles);
    }

    /**
     * Mettre à jour les rôles d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param roles La liste des rôles
     * @throws SQLException En cas d'erreur SQL
     */
    private void updateUserRoles(int userId, List<String> roles) throws SQLException {
        String query = "UPDATE user SET role = ? WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Joindre les rôles en une chaîne séparée par des virgules
            String roleString = String.join(",", roles);
            
            statement.setString(1, roleString);
            statement.setInt(2, userId);
            
            statement.executeUpdate();
        }
    }

    /**
     * Vérifier si un utilisateur a un rôle spécifique
     * @param user L'utilisateur
     * @param role Le rôle à vérifier
     * @return true si l'utilisateur a le rôle, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean hasRole(User user, String role) throws SQLException {
        if (user == null) {
            return false;
        }
        
        List<String> roles = getUserRoles(user.getId());
        return roles.contains(role);
    }

    /**
     * Vérifier si un utilisateur est un administrateur
     * @param user L'utilisateur
     * @return true si l'utilisateur est un administrateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean isAdmin(User user) throws SQLException {
        return hasRole(user, ROLE_ADMIN) || hasRole(user, ROLE_SUPER_ADMIN);
    }

    /**
     * Vérifier si un utilisateur est un super administrateur
     * @param user L'utilisateur
     * @return true si l'utilisateur est un super administrateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean isSuperAdmin(User user) throws SQLException {
        return hasRole(user, ROLE_SUPER_ADMIN);
    }

    /**
     * Vérifier si un utilisateur est un client
     * @param user L'utilisateur
     * @return true si l'utilisateur est un client, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean isClient(User user) throws SQLException {
        return hasRole(user, ROLE_CLIENT);
    }

    /**
     * Déterminer le type d'utilisateur (admin, super admin ou client)
     * @param user L'utilisateur
     * @return Le type d'utilisateur (ADMIN, SUPER_ADMIN, CLIENT ou null si aucun rôle)
     * @throws SQLException En cas d'erreur SQL
     */
    public String getUserType(User user) throws SQLException {
        if (user == null) {
            return null;
        }
        
        if (isSuperAdmin(user)) {
            return ROLE_SUPER_ADMIN;
        } else if (isAdmin(user)) {
            return ROLE_ADMIN;
        } else if (isClient(user)) {
            return ROLE_CLIENT;
        }
        
        return null;
    }
}
