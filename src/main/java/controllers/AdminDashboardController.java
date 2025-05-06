package controllers;

import entities.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.AuthService;
import services.EventService;
import services.ReservationService;
import services.RoleService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Text userInfoText;

    @FXML
    private Text userCountText;

    @FXML
    private Text eventCountText;

    @FXML
    private Text reservationCountText;


    @FXML
    private TableView<?> usersTable;

    @FXML
    private TextField userSearchField;

    private AuthService authService;
    private RoleService roleService;

    public AdminDashboardController() {
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Afficher les informations de l'utilisateur connecté
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            userInfoText.setText("Connecté en tant que: " + currentUser.getPrenom() + " " + currentUser.getNom());
        }

        // Charger les statistiques
        loadStatistics();

        // Initialiser les tableaux
        initializeTables();
    }

    private void loadStatistics() {
        try {
            // Compter le nombre d'utilisateurs
            int userCount = 0; // À implémenter
            userCountText.setText(String.valueOf(userCount));

            // Compter le nombre d'événements
            int eventCount = 0; // À implémenter
            eventCountText.setText(String.valueOf(eventCount));

            // Compter le nombre de réservations
            int reservationCount = 0; // À implémenter
            reservationCountText.setText(String.valueOf(reservationCount));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTables() {

    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        authService.logout();

        try {
            // Charger la page de connexion
            File file = new File("src/main/resources/fxml/Login.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) userInfoText.getScene().getWindow();
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

    @FXML
    public void handleExit(ActionEvent event) {
        // Quitter l'application
        Platform.exit();
    }

    @FXML
    public void handleManageUsers(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleManageEvents(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleManageReservations(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleAddEvent(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleAddUser(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
