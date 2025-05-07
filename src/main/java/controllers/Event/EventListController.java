package controllers.Event;

import entities.Event;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.AuthService;
import services.EventService;
import services.ReservationService;
import services.RoleService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EventListController implements Initializable {

    @FXML
    private ListView<Event> eventListView;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Text totalEventsText;

    private EventService eventService;
    private AuthService authService;
    private RoleService roleService;
    private ObservableList<Event> eventList;

    public EventListController() {
        eventService = EventService.getInstance();
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        eventList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer la ListView
        eventListView.setCellFactory(param -> new EventListCell());

        // Configurer le double-clic sur un élément de la liste
        eventListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selectedEvent = eventListView.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    viewEvent(selectedEvent);
                }
            }
        });

        // Initialiser le filtre de statut
        statusFilter.getItems().addAll("Tous", "actif", "annulé", "complet");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(event -> filterEvents());

        // Configurer le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents());

        // Charger les événements
        loadEvents();
    }

    // Classe interne pour personnaliser l'affichage des événements dans la ListView
    private class EventListCell extends ListCell<Event> {
        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);

            if (empty || event == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Créer un conteneur principal pour l'affichage de l'événement
                HBox mainContainer = new HBox(10);
                mainContainer.getStyleClass().add("event-card");

                // Conteneur pour les informations textuelles
                VBox infoContainer = new VBox(5);
                HBox.setHgrow(infoContainer, Priority.ALWAYS);

                // Titre de l'événement
                Label titleLabel = new Label(event.getTitle());
                titleLabel.getStyleClass().add("event-title");

                // Description de l'événement (tronquée si trop longue)
                String description = event.getDescription();
                if (description != null && description.length() > 50) {
                    description = description.substring(0, 47) + "...";
                }
                Label descriptionLabel = new Label(description);
                descriptionLabel.getStyleClass().add("event-description");

                // Dates de l'événement
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Label dateLabel = new Label("Du " + dateFormat.format(event.getDate_debut()) +
                                           " au " + dateFormat.format(event.getDate_fin()));
                dateLabel.getStyleClass().add("event-date");

                // Statut et organisateur
                HBox infoBox = new HBox(10);
                Label statusLabel = new Label("Statut: " + event.getStatus());
                statusLabel.getStyleClass().add(getStatusStyleClass(event.getStatus()));

                Label userLabel = new Label("Organisateur: " +
                                          (event.getUser() != null ?
                                           event.getUser().getPrenom() + " " + event.getUser().getNom() : ""));
                userLabel.getStyleClass().add("event-info");

                infoBox.getChildren().addAll(statusLabel, userLabel);

                // Ajouter les éléments au conteneur d'informations
                infoContainer.getChildren().addAll(titleLabel, descriptionLabel, dateLabel, infoBox);

                // Conteneur pour l'image et les boutons
                VBox imageContainer = new VBox(5);
                imageContainer.setPrefWidth(150);

                // Image de l'événement
                ImageView imageView = new ImageView();
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);

                // Charger l'image
                if (event.getImage() != null && !event.getImage().isEmpty()) {
                    try {
                        String imagePath = event.getImage();
                        // Vérifier si c'est une URL externe ou un chemin local
                        if (imagePath.startsWith("/images/")) {
                            // C'est un chemin local, construire l'URL complète
                            imagePath = "file:src/main/resources" + imagePath;
                        }

                        Image image = new Image(imagePath, 150, 100, true, true);
                        imageView.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                    }
                }

                imageContainer.getChildren().add(imageView);

                // Ajouter le conteneur d'informations et l'image au conteneur principal
                mainContainer.getChildren().addAll(infoContainer, imageContainer);

                // Ajouter des boutons d'action si l'utilisateur est admin ou organisateur
                try {
                    User currentUser = authService.getCurrentUser();
                    boolean isAdmin = roleService.isAdmin(currentUser);
                    boolean isOrganiser = event.getUser() != null &&
                            currentUser != null &&
                            event.getUser().getId() == currentUser.getId();

                    // Ajouter des boutons d'action
                    HBox actionBox = new HBox(5);

                    // Bouton Réserver (pour tous les utilisateurs)
                    Button reserveBtn = new Button("Réserver");
                    reserveBtn.getStyleClass().add("button-success");
                    reserveBtn.setOnAction(e -> reserveEvent(event));
                    actionBox.getChildren().add(reserveBtn);

                    // Boutons Modifier et Supprimer (pour admin et organisateur)
                    if (isAdmin || isOrganiser) {
                        Button editBtn = new Button("Modifier");
                        editBtn.getStyleClass().add("button-warning");
                        editBtn.setOnAction(e -> editEvent(event));

                        Button deleteBtn = new Button("Supprimer");
                        deleteBtn.getStyleClass().add("button-danger");
                        deleteBtn.setOnAction(e -> deleteEvent(event));

                        actionBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                    // Ajouter les boutons au conteneur d'image
                    imageContainer.getChildren().add(actionBox);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                setGraphic(mainContainer);
            }
        }
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            eventList.clear();
            eventList.addAll(events);
            eventListView.setItems(eventList);

            // Mettre à jour le compteur d'événements
            totalEventsText.setText("Total: " + events.size() + " événement(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des événements", e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterEvents() {
        String searchText = searchField.getText().toLowerCase();
        String statusText = statusFilter.getValue();

        try {
            List<Event> allEvents = eventService.getAllEvents();
            eventList.clear();

            for (Event event : allEvents) {
                boolean matchesSearch = searchText.isEmpty() ||
                        event.getTitle().toLowerCase().contains(searchText) ||
                        event.getDescription().toLowerCase().contains(searchText);

                boolean matchesStatus = "Tous".equals(statusText) ||
                        (event.getStatus() != null && event.getStatus().equals(statusText));

                if (matchesSearch && matchesStatus) {
                    eventList.add(event);
                }
            }

            // Mettre à jour le compteur d'événements
            totalEventsText.setText("Total: " + eventList.size() + " événement(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des événements", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddEvent(ActionEvent event) {
        try {
            File file = new File("src/main/resources/fxml/event/EventAdd.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Ajouter un événement");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();

                // Recharger les événements après l'ajout
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé", file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire d'ajout", e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewEvent(Event event) {
        try {
            File file = new File("src/main/resources/fxml/event/EventView.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                EventViewController controller = loader.getController();
                controller.setEvent(event);

                Stage stage = new Stage();
                stage.setTitle("Détails de l'événement");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé", file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails de l'événement", e.getMessage());
            e.printStackTrace();
        }
    }

    private void editEvent(Event event) {
        try {
            File file = new File("src/main/resources/fxml/event/EventEdit.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                EventEditController controller = loader.getController();
                controller.setEvent(event);

                Stage stage = new Stage();
                stage.setTitle("Modifier l'événement");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();

                // Recharger les événements après la modification
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé", file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de modification", e.getMessage());
            e.printStackTrace();
        }
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

    /**
     * Réserver un événement
     * @param event L'événement à réserver
     */
    private void reserveEvent(Event event) {
        try {
            // Vérifier si l'utilisateur est connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Connexion requise", "Vous devez être connecté pour réserver un événement");
                return;
            }

            // Vérifier si l'événement est disponible
            if (!"actif".equals(event.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Événement non disponible", "Cet événement n'est pas disponible pour réservation");
                return;
            }

            // Vérifier si l'utilisateur a déjà réservé cet événement
            ReservationService reservationService = ReservationService.getInstance();
            if (reservationService.hasUserReservedEvent(currentUser.getId(), event.getId())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Réservation existante", "Vous avez déjà réservé cet événement");
                return;
            }

            // Créer la réservation
            boolean success = reservationService.addReservation(currentUser.getId(), event.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation confirmée", "Votre réservation a été enregistrée avec succès");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de réservation", "Une erreur est survenue lors de la réservation");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur technique", "Erreur lors de la réservation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteEvent(Event event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer l'événement");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitle() + "\" ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                eventService.deleteEvent(event.getId());
                loadEvents();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé", "L'événement a été supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'événement", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleRefresh(ActionEvent event) {
        loadEvents();
    }

    @FXML
    public void handleClearFilters(ActionEvent event) {
        searchField.clear();
        statusFilter.setValue("Tous");
        loadEvents();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
