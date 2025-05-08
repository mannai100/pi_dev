package controllers;

import entities.Event;
import entities.ReserverEvent;
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

import java.util.List;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    // Instance statique pour permettre le rafraîchissement des statistiques depuis d'autres contrôleurs
    private static ClientDashboardController instance;

    @FXML
    private Text userInfoText;

    @FXML
    private Text availableEventsText;

    @FXML
    private Text myReservationsText;

    @FXML
    private TableView<?> eventsTable;

    @FXML
    private TableView<?> reservationsTable;

    @FXML
    private TextField eventSearchField;

    @FXML
    private Text nameText;

    @FXML
    private Text firstNameText;

    @FXML
    private Text emailText;

    @FXML
    private Text addressText;

    @FXML
    private Text phoneText;

    private AuthService authService;
    private RoleService roleService;
    private EventService eventService;
    private ReservationService reservationService;

    public ClientDashboardController() {
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        eventService = EventService.getInstance();
        reservationService = ReservationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stocker l'instance pour permettre le rafraîchissement des statistiques
        instance = this;

        // Afficher les informations de l'utilisateur connecté
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            userInfoText.setText("Connecté en tant que: " + currentUser.getPrenom() + " " + currentUser.getNom());

            // Remplir les informations du profil
            nameText.setText(currentUser.getNom());
            firstNameText.setText(currentUser.getPrenom());
            emailText.setText(currentUser.getEmail());
            addressText.setText(currentUser.getAdresse());
            phoneText.setText(currentUser.getTelephone());
        }

        // Charger les statistiques
        loadStatistics();

        // Initialiser les tableaux
        initializeTables();
    }

    /**
     * Charge les statistiques (nombre d'événements disponibles et nombre de réservations)
     * Méthode privée utilisée en interne
     */
    private void loadStatistics() {
        try {
            // Compter le nombre d'événements disponibles
            List<Event> events = eventService.getAllEvents();
            int availableEvents = 0;

            // Compter uniquement les événements actifs
            for (Event event : events) {
                if (event.getStatus() != null && event.getStatus().equals("actif")) {
                    availableEvents++;
                }
            }

            availableEventsText.setText(String.valueOf(availableEvents));

            // Compter le nombre de réservations de l'utilisateur
            User currentUser = authService.getCurrentUser();
            int myReservations = 0;

            if (currentUser != null) {
                List<ReserverEvent> reservations = reservationService.getReservationsByUser(currentUser.getId());
                myReservations = reservations.size();
            }

            myReservationsText.setText(String.valueOf(myReservations));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit les statistiques (nombre d'événements disponibles et nombre de réservations)
     * Méthode publique pouvant être appelée depuis d'autres contrôleurs
     */
    public void refreshStatistics() {
        loadStatistics();
    }

    /**
     * Méthode statique pour rafraîchir les statistiques depuis n'importe quel contrôleur
     * Cette méthode peut être appelée après l'ajout, la modification ou la suppression d'un événement
     */
    public static void refreshDashboardStatistics() {
        if (instance != null) {
            instance.refreshStatistics();
        }
    }

    private void initializeTables() {
        // Initialiser le tableau des événements
        // À implémenter

        // Initialiser le tableau des réservations
        // À implémenter
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
    public void handleSearchEvents(ActionEvent event) {
        try {
            File file = new File("src/main/resources/fxml/event/EventList.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Liste des événements");
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la page des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMyReservations(ActionEvent event) {
        try {
            File file = new File("src/main/resources/fxml/event/ReservationList.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Mes réservations");
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la page des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewProfile(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleEditProfile(ActionEvent event) {
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
