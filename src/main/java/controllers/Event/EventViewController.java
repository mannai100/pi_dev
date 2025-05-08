package controllers.Event;

import entities.Event;
import entities.ReserverEvent;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.AuthService;
import services.ReservationService;
import services.RoleService;
import controllers.ClientDashboardController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class EventViewController implements Initializable {

    @FXML
    private Text titleLabel;

    @FXML
    private Text descriptionText;

    @FXML
    private Label dateDebutLabel;

    @FXML
    private Label dateFinLabel;

    // Le champ maxParticipantsLabel a été supprimé

    @FXML
    private Label statusLabel;

    @FXML
    private Rectangle imagePlaceholder;

    @FXML
    private Text noImageText;

    @FXML
    private Label userLabel;

    @FXML
    private ImageView eventImageView;

    @FXML
    private Button reserveButton;

    @FXML
    private Button editButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button avisButton;

    private Event event;
    private AuthService authService;
    private RoleService roleService;
    private ReservationService reservationService;

    public EventViewController() {
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        reservationService = ReservationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Les contrôles seront initialisés dans la méthode setEvent
    }

    public void setEvent(Event event) {
        this.event = event;

        // Afficher les informations de l'événement
        titleLabel.setText(event.getTitle());
        descriptionText.setText(event.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateDebutLabel.setText(dateFormat.format(event.getDate_debut()));
        dateFinLabel.setText(dateFormat.format(event.getDate_fin()));

        // La ligne pour définir la valeur du label max_participants a été supprimée
        statusLabel.setText(event.getStatus());

        User user = event.getUser();
        userLabel.setText(user != null ? user.getPrenom() + " " + user.getNom() : "");

        // Afficher l'image de l'événement si disponible
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                String imagePath = event.getImage();
                // Vérifier si c'est une URL externe ou un chemin local
                if (imagePath.startsWith("/images/")) {
                    // C'est un chemin local, construire l'URL complète
                    imagePath = "file:src/main/resources" + imagePath;
                }

                Image image = new Image(imagePath);
                eventImageView.setImage(image);
                imagePlaceholder.setVisible(false);
                noImageText.setVisible(false);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                imagePlaceholder.setVisible(true);
                noImageText.setVisible(true);
            }
        } else {
            imagePlaceholder.setVisible(true);
            noImageText.setVisible(true);
        }

        // Appliquer le style approprié au statut
        String statusStyleClass = getStatusStyleClass(event.getStatus());
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(statusStyleClass);

        // Configurer les boutons en fonction des droits de l'utilisateur
        try {
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);
            boolean isOrganiser = event.getUser() != null &&
                    currentUser != null &&
                    event.getUser().getId() == currentUser.getId();

            // Seuls l'admin et l'organisateur peuvent modifier l'événement
            editButton.setVisible(isAdmin || isOrganiser);

            // Vérifier si l'utilisateur a déjà réservé cet événement
            boolean hasReserved = false;
            if (currentUser != null) {
                hasReserved = reservationService.hasUserReservedEvent(currentUser.getId(), event.getId());
            }

            // L'organisateur ne peut pas réserver son propre événement
            reserveButton.setVisible(currentUser != null && !isOrganiser && !hasReserved);

            // Désactiver le bouton de réservation si l'événement est complet ou annulé
            if ("complet".equals(event.getStatus()) || "annulé".equals(event.getStatus())) {
                reserveButton.setDisable(true);
                reserveButton.setText("Indisponible");
                reserveButton.getStyleClass().clear();
                reserveButton.getStyleClass().add("button-danger");
            } else if (hasReserved) {
                reserveButton.setDisable(true);
                reserveButton.setText("Déjà réservé");
                reserveButton.getStyleClass().clear();
                reserveButton.getStyleClass().add("button-warning");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReserve(ActionEvent event) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour réserver un événement");
                return;
            }

            // Enregistrer la réservation
            boolean success = reservationService.addReservation(currentUser.getId(), this.event.getId());

            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la réservation");
                return;
            }

            // Rafraîchir les statistiques du tableau de bord
            ClientDashboardController.refreshDashboardStatistics();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre réservation a été enregistrée avec succès");

            // Désactiver le bouton de réservation
            reserveButton.setDisable(true);
            reserveButton.setText("Déjà réservé");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la réservation: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        try {
            File file = new File("src/main/resources/fxml/event/EventEdit.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                EventEditController controller = loader.getController();
                controller.setEvent(this.event);

                Stage stage = new Stage();
                stage.setTitle("Modifier l'événement");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();

                // Fermer cette fenêtre
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
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

    /**
     * Obtenir la classe de style CSS en fonction du statut de l'événement
     * @param status Le statut de l'événement
     * @return La classe de style CSS correspondante
     */
    private String getStatusStyleClass(String status) {
        if (status == null) {
            return "status-pending";
        }

        switch (status) {
            case "actif":
                return "status-active";
            case "annulé":
                return "status-cancelled";
            case "complet":
                return "status-completed";
            default:
                return "status-pending";
        }
    }

    @FXML
    public void handleAvis() {
        try {
            // Charger la vue des avis
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/AvisView.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur
            AvisController controller = loader.getController();
            
            // Passer l'événement au contrôleur
            controller.setEvent(event);
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            
            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Avis - " + event.getTitle());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(avisButton.getScene().getWindow());
            
            // Afficher la fenêtre
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la vue des avis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
