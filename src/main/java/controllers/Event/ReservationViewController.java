package controllers.Event;

import entities.Event;
import entities.ReserverEvent;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.AuthService;
import services.ReservationService;
import services.RoleService;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class ReservationViewController implements Initializable {

    @FXML
    private Label eventTitleLabel;

    @FXML
    private Label eventDescriptionLabel;

    @FXML
    private Label eventDateDebutLabel;

    @FXML
    private Label eventDateFinLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private Label dateReservationLabel;

    @FXML
    private Label statutLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button closeButton;

    private ReserverEvent reservation;
    private ReservationService reservationService;
    private AuthService authService;
    private RoleService roleService;

    public ReservationViewController() {
        reservationService = ReservationService.getInstance();
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Les contrôles seront initialisés dans la méthode setReservation
    }

    public void setReservation(ReserverEvent reservation) {
        this.reservation = reservation;
        
        // Afficher les informations de l'événement
        Event event = reservation.getEvent();
        if (event != null) {
            eventTitleLabel.setText(event.getTitle());
            eventDescriptionLabel.setText(event.getDescription());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            eventDateDebutLabel.setText(dateFormat.format(event.getDate_debut()));
            eventDateFinLabel.setText(dateFormat.format(event.getDate_fin()));
        }
        
        // Afficher les informations de l'utilisateur
        User user = reservation.getUser();
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userEmailLabel.setText(user.getEmail());
        }
        
        // Afficher les informations de la réservation
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateReservationLabel.setText(dateFormat.format(reservation.getDateReservation()));
        statutLabel.setText(reservation.getStatut());
        
        // Configurer les boutons en fonction des droits de l'utilisateur
        try {
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);
            boolean isOrganiser = event != null && 
                    event.getUser() != null && 
                    currentUser != null && 
                    event.getUser().getId() == currentUser.getId();
            
            // Seuls l'admin et l'organisateur peuvent confirmer ou annuler
            boolean canManage = isAdmin || isOrganiser;
            confirmButton.setVisible(canManage && "en attente".equals(reservation.getStatut()));
            cancelButton.setVisible(canManage || 
                    (user != null && currentUser != null && user.getId() == currentUser.getId() && "en attente".equals(reservation.getStatut())));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleConfirm(ActionEvent event) {
        try {
            reservationService.updateReservationStatus(reservation.getId(), "confirmé");
            statutLabel.setText("confirmé");
            confirmButton.setVisible(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "La réservation a été confirmée avec succès");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la confirmation de la réservation: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            reservationService.updateReservationStatus(reservation.getId(), "annulé");
            statutLabel.setText("annulé");
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "La réservation a été annulée avec succès");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'annulation de la réservation: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
