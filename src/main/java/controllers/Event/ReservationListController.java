package controllers.Event;

import entities.Event;
import entities.ReserverEvent;
import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AuthService;
import services.ReservationService;
import services.RoleService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReservationListController implements Initializable {

    @FXML
    private ListView<ReserverEvent> reservationListView;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statutFilter;

    @FXML
    private Text totalReservationsText;

    @FXML
    private VBox noReservationsBox;

    private ReservationService reservationService;
    private AuthService authService;
    private RoleService roleService;
    private ObservableList<ReserverEvent> reservationList;

    public ReservationListController() {
        reservationService = ReservationService.getInstance();
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        reservationList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer la ListView
        reservationListView.setCellFactory(param -> new ReservationListCell());

        // Configurer le double-clic sur un élément de la liste
        reservationListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ReserverEvent selectedReservation = reservationListView.getSelectionModel().getSelectedItem();
                if (selectedReservation != null) {
                    viewReservation(selectedReservation);
                }
            }
        });

        // Initialiser le filtre de statut
        statutFilter.getItems().addAll("Tous", "en attente", "confirmé", "annulé");
        statutFilter.setValue("Tous");
        statutFilter.setOnAction(event -> filterReservations());

        // Configurer le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterReservations());

        // Initialiser le message "Aucune réservation"
        noReservationsBox.setVisible(false);

        // Charger les réservations
        loadReservations();
    }

    // Classe interne pour personnaliser l'affichage des réservations dans la ListView
    private class ReservationListCell extends ListCell<ReserverEvent> {
        @Override
        protected void updateItem(ReserverEvent reservation, boolean empty) {
            super.updateItem(reservation, empty);

            if (empty || reservation == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Créer un conteneur pour l'affichage de la réservation
                VBox vbox = new VBox(5);

                // Événement réservé
                Event event = reservation.getEvent();
                Label eventLabel = new Label(event != null ? event.getTitle() : "");
                eventLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // Utilisateur qui a réservé
                User user = reservation.getUser();
                Label userLabel = new Label("Réservé par: " + (user != null ? user.getPrenom() + " " + user.getNom() : ""));

                // Date de réservation
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Label dateLabel = new Label("Date de réservation: " + dateFormat.format(reservation.getDateReservation()));

                // Statut
                Label statutLabel = new Label("Statut: " + reservation.getStatut());
                statutLabel.setStyle(getStatutStyle(reservation.getStatut()));

                // Ajouter les éléments au conteneur
                vbox.getChildren().addAll(eventLabel, userLabel, dateLabel, statutLabel);

                // Ajouter des boutons d'action en fonction des droits de l'utilisateur
                try {
                    User currentUser = authService.getCurrentUser();
                    boolean isAdmin = roleService.isAdmin(currentUser);
                    boolean isOrganiser = event != null &&
                            event.getUser() != null &&
                            currentUser != null &&
                            event.getUser().getId() == currentUser.getId();
                    boolean isOwner = user != null &&
                            currentUser != null &&
                            user.getId() == currentUser.getId();

                    HBox actionBox = new HBox(5);

                    Button viewBtn = new Button("Voir");
                    viewBtn.setOnAction(e -> viewReservation(reservation));
                    actionBox.getChildren().add(viewBtn);

                    // Seuls l'admin et l'organisateur peuvent confirmer ou annuler
                    if (isAdmin || isOrganiser) {
                        if ("en attente".equals(reservation.getStatut())) {
                            Button confirmBtn = new Button("Confirmer");
                            confirmBtn.setOnAction(e -> confirmReservation(reservation));
                            actionBox.getChildren().add(confirmBtn);
                        }

                        Button cancelBtn = new Button("Annuler");
                        cancelBtn.setOnAction(e -> cancelReservation(reservation));
                        actionBox.getChildren().add(cancelBtn);
                    } else if (isOwner && "en attente".equals(reservation.getStatut())) {
                        // Le propriétaire de la réservation peut l'annuler si elle est en attente
                        Button cancelBtn = new Button("Annuler");
                        cancelBtn.setOnAction(e -> cancelReservation(reservation));
                        actionBox.getChildren().add(cancelBtn);
                    }

                    vbox.getChildren().add(actionBox);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                setGraphic(vbox);
            }
        }

        private String getStatutStyle(String statut) {
            switch (statut) {
                case "confirmé":
                    return "-fx-text-fill: green; -fx-font-weight: bold;";
                case "annulé":
                    return "-fx-text-fill: red; -fx-font-weight: bold;";
                case "en attente":
                    return "-fx-text-fill: orange; -fx-font-weight: bold;";
                default:
                    return "";
            }
        }
    }

    private void loadReservations() {
        try {
            User currentUser = authService.getCurrentUser();
            List<ReserverEvent> reservations;

            if (currentUser != null) {
                boolean isAdmin = roleService.isAdmin(currentUser);

                if (isAdmin) {
                    // Les administrateurs voient toutes les réservations
                    reservations = reservationService.getAllReservations();
                } else {
                    // Les utilisateurs normaux voient leurs propres réservations
                    reservations = reservationService.getReservationsByUser(currentUser.getId());
                }

                reservationList.clear();
                reservationList.addAll(reservations);
                reservationListView.setItems(reservationList);

                // Mettre à jour le compteur
                totalReservationsText.setText("Total: " + reservations.size() + " réservation(s)");

                // Afficher ou masquer le message "Aucune réservation"
                noReservationsBox.setVisible(reservations.isEmpty());
                reservationListView.setVisible(!reservations.isEmpty());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réservations", e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterReservations() {
        String searchText = searchField.getText().toLowerCase();
        String statutText = statutFilter.getValue();

        try {
            User currentUser = authService.getCurrentUser();
            List<ReserverEvent> allReservations;

            if (currentUser != null) {
                boolean isAdmin = roleService.isAdmin(currentUser);

                if (isAdmin) {
                    // Les administrateurs voient toutes les réservations
                    allReservations = reservationService.getAllReservations();
                } else {
                    // Les utilisateurs normaux voient leurs propres réservations
                    allReservations = reservationService.getReservationsByUser(currentUser.getId());
                }

                reservationList.clear();

                for (ReserverEvent reservation : allReservations) {
                    boolean matchesSearch = searchText.isEmpty() ||
                            (reservation.getEvent() != null && reservation.getEvent().getTitle().toLowerCase().contains(searchText)) ||
                            (reservation.getUser() != null && (reservation.getUser().getNom().toLowerCase().contains(searchText) ||
                                                              reservation.getUser().getPrenom().toLowerCase().contains(searchText)));

                    boolean matchesStatut = "Tous".equals(statutText) ||
                            (reservation.getStatut() != null && reservation.getStatut().equals(statutText));

                    if (matchesSearch && matchesStatut) {
                        reservationList.add(reservation);
                    }
                }

                // Mettre à jour la liste
                reservationListView.setItems(reservationList);

                // Mettre à jour le compteur
                totalReservationsText.setText("Total: " + reservationList.size() + " réservation(s)");

                // Afficher ou masquer le message "Aucune réservation"
                noReservationsBox.setVisible(reservationList.isEmpty());
                reservationListView.setVisible(!reservationList.isEmpty());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des réservations", e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewReservation(ReserverEvent reservation) {
        try {
            File file = new File("src/main/resources/fxml/event/ReservationView.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                ReservationViewController controller = loader.getController();
                controller.setReservation(reservation);

                Stage stage = new Stage();
                stage.setTitle("Détails de la réservation");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();

                // Recharger les réservations après la fermeture de la fenêtre
                loadReservations();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé", file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails de la réservation", e.getMessage());
            e.printStackTrace();
        }
    }

    private void confirmReservation(ReserverEvent reservation) {
        try {
            reservationService.updateReservationStatus(reservation.getId(), "confirmé");
            loadReservations();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "La réservation a été confirmée avec succès");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la confirmation de la réservation", e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        }
    }

    private void cancelReservation(ReserverEvent reservation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation d'annulation");
        confirmDialog.setHeaderText("Annuler la réservation");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reservationService.updateReservationStatus(reservation.getId(), "annulé");
                loadReservations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La réservation a été annulée avec succès");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'annulation de la réservation", e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
            }
        }
    }

    @FXML
    public void handleRefresh(ActionEvent event) {
        loadReservations();
    }

    @FXML
    public void handleClearFilters(ActionEvent event) {
        searchField.clear();
        statutFilter.setValue("Tous");
        loadReservations();
    }

    @FXML
    public void handleViewEvents(ActionEvent event) {
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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        showAlert(alertType, title, null, content);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
