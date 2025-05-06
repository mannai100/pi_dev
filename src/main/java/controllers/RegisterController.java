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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    private AuthService authService;

    public RegisterController() {
        // Initialiser le service d'authentification
        authService = AuthService.getInstance();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        // Récupérer les valeurs des champs
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();

        // Vérifier que les champs ne sont pas vides
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || adresse.isEmpty() || telephone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Vérifier si l'email existe déjà
            if (authService.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Cet email est déjà utilisé.");
                return;
            }

            // Vérifier si le téléphone existe déjà
            if (authService.telephoneExists(telephone)) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Ce numéro de téléphone est déjà utilisé.");
                return;
            }

            // Créer un nouvel utilisateur
            User user = new User();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setPassword(password);
            user.setAdresse(adresse);
            user.setTelephone(telephone);
            user.setVerified(false);
            user.setCreated_at(new Timestamp(System.currentTimeMillis()));

            // Enregistrer l'utilisateur avec le service d'authentification
            authService.register(user);

            // Afficher un message de succès
            showAlert(Alert.AlertType.INFORMATION, "Inscription réussie", "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");

            // Rediriger vers la page de connexion
            navigateToLogin();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Une erreur est survenue lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            // Charger la page de connexion
            File file = new File("src/main/resources/fxml/Login.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) loginLink.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }



    private void navigateToLogin() throws IOException {
        // Charger la page de connexion
        File file = new File("src/main/resources/fxml/Login.fxml");
        if (file.exists()) {
            URL url = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Configurer la scène
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
