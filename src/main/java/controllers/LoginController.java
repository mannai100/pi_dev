package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import services.AuthService;
import services.RoleService;
import org.jboss.aerogear.security.otp.Totp;

import java.util.Optional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

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
                // Vérifier si l'utilisateur a activé l'authentification à deux facteurs
                if (user.getSecretKey() != null && !user.getSecretKey().isEmpty()) {
                    // Rediriger vers la page de vérification 2FA
                    showVerification2FADialog(user);
                } else {
                    // Connexion réussie sans 2FA
                    showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + user.getPrenom() + " " + user.getNom() + "!");

                    // Rediriger vers le tableau de bord approprié en fonction du rôle
                    navigateToDashboard(user);
                }
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



    private void navigateToDashboard(User user) throws IOException {
        try {
            // Déterminer le type d'utilisateur
            String userType = roleService.getUserType(user);
            String fxmlPath;
            String title;

            // Choisir le tableau de bord approprié
            if (userType != null && (userType.equals(RoleService.ROLE_ADMIN) || userType.equals(RoleService.ROLE_SUPER_ADMIN))) {
                fxmlPath = "src/main/resources/fxml/AdminDashboard.fxml";
                title = "Tableau de bord administrateur";
            } else {
                fxmlPath = "src/main/resources/fxml/HomePage.fxml";
                title = "Tableau de bord client";
            }

            // Charger le fichier FXML
            File file = new File(fxmlPath);
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(title);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
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
     * Affiche une boîte de dialogue pour la vérification 2FA
     * @param user L'utilisateur qui se connecte
     */
    private void showVerification2FADialog(User user) {
        try {
            // Créer une boîte de dialogue pour la vérification 2FA
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Vérification à deux facteurs");
            dialog.setHeaderText("Veuillez entrer le code généré par votre application d'authentification");

            // Configurer les boutons
            ButtonType validateButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

            // Créer le champ de saisie du code
            TextField codeField = new TextField();
            codeField.setPromptText("Code à 6 chiffres");

            // Créer la mise en page
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Code:"), 0, 0);
            grid.add(codeField, 1, 0);

            dialog.getDialogPane().setContent(grid);

            // Convertir le résultat en code
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == validateButtonType) {
                    return codeField.getText();
                }
                return null;
            });

            // Attendre la réponse de l'utilisateur
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(code -> {
                try {
                    // Vérifier le code 2FA
                    if (verify2FACode(user, code)) {
                        // Code valide, connexion réussie
                        showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + user.getPrenom() + " " + user.getNom() + "!");

                        // Rediriger vers le tableau de bord approprié
                        navigateToDashboard(user);
                    } else {
                        // Code invalide
                        showAlert(Alert.AlertType.ERROR, "Erreur de vérification", "Code invalide. Veuillez réessayer.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la vérification: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifie le code 2FA
     * @param user L'utilisateur qui se connecte
     * @param code Le code entré par l'utilisateur
     * @return true si le code est valide, false sinon
     */
    private boolean verify2FACode(User user, String code) {
        try {
            // Vérifier que le code n'est pas vide
            if (code == null || code.trim().isEmpty() || user.getSecretKey() == null) {
                return false;
            }

            // Vérifier le code avec la clé secrète de l'utilisateur
            Totp totp = new Totp(user.getSecretKey());
            return totp.verify(code);

        } catch (Exception e) {
            System.err.println("Exception dans verify2FACode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
