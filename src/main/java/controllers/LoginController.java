package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;

import services.AuthService;
import services.RoleService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    private AuthService authService;
    private RoleService roleService;

    public LoginController() {
        // Initialiser les services
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Vérifier que les champs ne sont pas vides
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Vérifier les identifiants avec le service d'authentification
            User user = authService.login(email, password);

            if (user != null) {
                // Vérifier si le compte de l'utilisateur est vérifié
                if (!user.isVerified()) {
                    // Demander à l'utilisateur s'il souhaite configurer l'authentification à deux facteurs maintenant
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Compte non vérifié");
                    alert.setHeaderText("Votre compte n'a pas encore été vérifié");
                    alert.setContentText("Souhaitez-vous configurer l'authentification à deux facteurs maintenant ?");

                    ButtonType buttonTypeYes = new ButtonType("Oui");
                    ButtonType buttonTypeNo = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == buttonTypeYes) {
                        // Rediriger vers la page de configuration 2FA
                        navigateTo2FASetup(user.getEmail());
                    }
                    return;
                }

                // Connexion réussie (2FA désactivé)
                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + user.getPrenom() + " " + user.getNom() + "!");

                // Rediriger vers le tableau de bord approprié en fonction du rôle
                navigateToDashboard(user);
            } else {
                // Échec de la connexion
                showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            // Charger la page d'inscription avec un chemin absolu
            File file = new File("src/main/resources/fxml/Register.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) registerLink.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Inscription");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page d'inscription.");
            e.printStackTrace();
        }
    }

    /**
     * Gère le clic sur le lien "Mot de passe oublié"
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            // Charger la page de réinitialisation de mot de passe
            File file = new File("src/main/resources/fxml/ResetPassword.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Réinitialisation du mot de passe");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Erreur lors du chargement de la page de réinitialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) throws IOException {
        try {
            // Déterminer le type d'utilisateur
            String userType = roleService.getUserType(user);
            String title;

            // Choisir le tableau de bord approprié
            String fxmlPath;
            if (userType != null && (userType.equals(RoleService.ROLE_ADMIN) || userType.equals(RoleService.ROLE_SUPER_ADMIN))) {
                fxmlPath = "/fxml/admin/AdminDashboard.fxml";
                title = "Panneau d'administration";
            } else {
                fxmlPath = "/fxml/ClientDashboard.fxml";
                title = "Tableau de bord client";
            }

            // Obtenir l'URL du fichier FXML
            URL url = getClass().getResource(fxmlPath);
            System.out.println("URL du fichier FXML: " + url);

            if (url == null) {
                System.err.println("Impossible de trouver le fichier FXML: " + fxmlPath);

                // Essayer avec un chemin absolu
                File file = new File("src/main/resources" + fxmlPath);
                if (file.exists()) {
                    url = file.toURI().toURL();
                    System.out.println("URL créée à partir du fichier: " + url);
                } else {
                    throw new IOException("Fichier FXML introuvable: " + fxmlPath);
                }
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Configurer la scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de rôle", "Impossible de déterminer le rôle de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Navigue vers la page de configuration 2FA
     * @param email L'email de l'utilisateur
     */
    private void navigateTo2FASetup(String email) {
        try {
            // Charger la page de configuration 2FA
            File file = new File("src/main/resources/fxml/2fa.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Récupérer le contrôleur et lui passer l'email
                controller2fa controller = loader.getController();
                controller.setEmail(email);

                // Configurer la scène
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Configuration de l'authentification à deux facteurs");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Erreur lors du chargement de la page 2FA: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
