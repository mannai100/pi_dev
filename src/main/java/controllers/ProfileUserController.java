package controllers;

import entities.User;
import entities.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Optional;
import java.sql.SQLException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileUserController implements Initializable {

    @FXML
    private ImageView profileImage;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField addressField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button setup2FAButton;

    private User currentUser;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service utilisateur
        userService = UserService.getInstance();

        // Récupérer l'utilisateur connecté depuis UserSession
        currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Remplir les champs avec les informations de l'utilisateur
            loadUserData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté");
        }

        // Configurer les actions des boutons
        setupButtonActions();
    }

    /**
     * Charge les données de l'utilisateur dans les champs du formulaire
     */
    private void loadUserData() {
        // Remplir les champs de texte
        lastNameField.setText(currentUser.getNom());
        firstNameField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone());
        addressField.setText(currentUser.getAdresse());

        // Charger l'image de profil si disponible
        if (currentUser.getImageUrl() != null && !currentUser.getImageUrl().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getImageUrl());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImage.setImage(image);
                } else {
                    // Charger une image par défaut
                    loadDefaultProfileImage();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                loadDefaultProfileImage();
            }
        } else {
            // Charger une image par défaut
            loadDefaultProfileImage();
        }

        // Note: La date de naissance n'est pas dans le modèle User actuel
        // Si vous ajoutez ce champ plus tard, vous pourrez le remplir ici
        // birthDatePicker.setValue(currentUser.getBirthDate());
    }

    /**
     * Charge une image de profil par défaut
     */
    private void loadDefaultProfileImage() {
        try {
            // Charger une image par défaut depuis les ressources
            // Essayer plusieurs chemins possibles
            URL defaultImageUrl = getClass().getResource("/images/default-profile.png");

            if (defaultImageUrl == null) {
                // Essayer un autre chemin
                defaultImageUrl = getClass().getClassLoader().getResource("images/default-profile.png");
            }

            if (defaultImageUrl == null) {
                // Essayer un chemin absolu
                File file = new File("src/main/resources/images/default-profile.png");
                if (file.exists()) {
                    defaultImageUrl = file.toURI().toURL();
                }
            }

            if (defaultImageUrl != null) {
                Image defaultImage = new Image(defaultImageUrl.toString());
                profileImage.setImage(defaultImage);
            } else {
                System.err.println("Image par défaut introuvable");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les actions des boutons
     */
    private void setupButtonActions() {
        // Action du bouton Enregistrer
        saveButton.setOnAction(event -> handleSave());

        // Action du bouton Annuler
        cancelButton.setOnAction(event -> handleCancel());

        // Action du bouton Modifier mot de passe
        changePasswordButton.setOnAction(event -> handleChangePassword());

        // Action du bouton Configurer 2FA
        setup2FAButton.setOnAction(event -> handleSetup2FA());

        // Action pour changer l'image de profil
        profileImage.setOnMouseClicked(event -> handleChangeProfileImage());
    }

    /**
     * Gère l'action du bouton Enregistrer
     */
    @FXML
    private void handleSave() {
        try {
            // Mettre à jour les informations de l'utilisateur
            currentUser.setNom(lastNameField.getText());
            currentUser.setPrenom(firstNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setTelephone(phoneField.getText());
            currentUser.setAdresse(addressField.getText());

            try {
                // Vérifier si l'email existe déjà pour un autre utilisateur
                if (userService.emailExistsForOtherUser(emailField.getText(), currentUser.getId())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé par un autre utilisateur");
                    return;
                }

                // Enregistrer les modifications dans la base de données
                userService.updateUser(currentUser);

                // Mettre à jour l'utilisateur dans la session
                UserSession.getInstance().setCurrentUser(currentUser);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du profil: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action du bouton Annuler
     */
    @FXML
    private void handleCancel() {
        // Recharger les données originales
        loadUserData();
    }

    /**
     * Gère l'action du bouton Modifier mot de passe
     */
    @FXML
    private void handleChangePassword() {
        // Créer une boîte de dialogue pour le changement de mot de passe
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le mot de passe");
        dialog.setHeaderText("Veuillez entrer votre mot de passe actuel et votre nouveau mot de passe");

        // Configurer les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Créer les champs de saisie
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Mot de passe actuel");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");

        // Créer la mise en page
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Mot de passe actuel:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("Nouveau mot de passe:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirmer le mot de passe:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Donner le focus au premier champ
        Platform.runLater(currentPasswordField::requestFocus);

        // Attendre la réponse de l'utilisateur
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Vérifier que tous les champs sont remplis
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis");
                return;
            }

            // Vérifier que le nouveau mot de passe et la confirmation correspondent
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nouveau mot de passe et sa confirmation ne correspondent pas");
                return;
            }

            // Vérifier que le mot de passe actuel est correct
            if (!currentPassword.equals(currentUser.getPassword())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe actuel est incorrect");
                return;
            }

            try {
                // Mettre à jour le mot de passe dans la base de données
                userService.updatePassword(currentUser.getId(), newPassword);

                // Mettre à jour le mot de passe dans l'objet utilisateur
                currentUser.setPassword(newPassword);

                // Mettre à jour l'utilisateur dans la session
                UserSession.getInstance().setCurrentUser(currentUser);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été modifié avec succès");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'action de changement d'image de profil
     */
    @FXML
    private void handleChangeProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Afficher la boîte de dialogue de sélection de fichier
        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Charger l'image sélectionnée
                Image image = new Image(selectedFile.toURI().toString());
                profileImage.setImage(image);

                // Mettre à jour le chemin de l'image dans l'objet utilisateur
                currentUser.setImageUrl(selectedFile.getAbsolutePath());

                try {
                    // Enregistrer le chemin de l'image dans la base de données
                    userService.updateUser(currentUser);

                    // Mettre à jour l'utilisateur dans la session
                    UserSession.getInstance().setCurrentUser(currentUser);

                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre image de profil a été mise à jour avec succès");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de l'image de profil: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'action du bouton Configurer l'authentification à deux facteurs
     */
    @FXML
    private void handleSetup2FA() {
        try {
            // Charger la vue 2FA
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/2fa.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et lui passer l'email de l'utilisateur
            controller2fa controller = loader.getController();
            controller.setEmail(currentUser.getEmail());

            // Afficher la fenêtre 2FA
            Stage stage = new Stage();
            stage.setTitle("Configuration de l'authentification à deux facteurs");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page 2FA : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de configuration 2FA : " + e.getMessage());
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
