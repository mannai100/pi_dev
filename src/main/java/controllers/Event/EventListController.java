package controllers.Event;

import entities.Event;
import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Separator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import services.AuthService;
import services.EventService;
import services.ReservationService;
import services.RoleService;
import controllers.ClientDashboardController;
import javafx.stage.Modality;
import controllers.Event.AvisController;
import services.HolidayService;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private VBox eventsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Text totalEventsText;

    @FXML
    private VBox holidayNoticeBox;

    @FXML
    private Button showHolidaysBtn;

    private EventService eventService;
    private AuthService authService;
    private RoleService roleService;
    private ObservableList<Event> eventList;
    private HolidayService holidayService;

    public EventListController() {
        eventService = EventService.getInstance();
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        eventList = FXCollections.observableArrayList();
        holidayService = HolidayService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le filtre de statut avec seulement les statuts pertinents pour le client
        statusFilter.getItems().addAll("Tous", "accepté", "en attente", "rejeté");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(event -> filterEvents());

        // Configurer le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents());

        // Afficher la notification des jours fériés
        holidayNoticeBox.setVisible(true);

        // Charger les événements
        loadEvents();
    }

    /**
     * Gérer le clic sur le bouton "Réserver"
     */
    @FXML
    public void handleReserveEvent(ActionEvent event) {
        // Récupérer l'événement associé au bouton
        Button button = (Button) event.getSource();
        Event selectedEvent = (Event) button.getUserData();

        if (selectedEvent != null) {
            reserveEvent(selectedEvent);
        } else {
            // Pour les boutons de test, utiliser le premier événement de la liste
            try {
                List<Event> events = eventService.getAllEvents();
                if (!events.isEmpty()) {
                    reserveEvent(events.get(0));
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun événement", "Aucun événement disponible pour la réservation.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de réservation", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gérer le clic sur le bouton "Modifier"
     */
    @FXML
    public void handleEditEvent(ActionEvent event) {
        // Récupérer l'événement associé au bouton
        Button button = (Button) event.getSource();
        Event selectedEvent = (Event) button.getUserData();

        if (selectedEvent != null) {
            editEvent(selectedEvent);
        } else {
            // Pour les boutons de test, utiliser le premier événement de la liste
            try {
                List<Event> events = eventService.getAllEvents();
                if (!events.isEmpty()) {
                    editEvent(events.get(0));
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun événement", "Aucun événement disponible pour la modification.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gérer le clic sur le bouton "Supprimer"
     */
    @FXML
    public void handleDeleteEvent(ActionEvent event) {
        // Récupérer l'événement associé au bouton
        Button button = (Button) event.getSource();
        Event selectedEvent = (Event) button.getUserData();

        if (selectedEvent != null) {
            deleteEvent(selectedEvent);
        } else {
            // Pour les boutons de test, utiliser le premier événement de la liste
            try {
                List<Event> events = eventService.getAllEvents();
                if (!events.isEmpty()) {
                    deleteEvent(events.get(0));
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun événement", "Aucun événement disponible pour la suppression.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Classe interne pour personnaliser l'affichage des événements dans la ListView
    private class EventListCell extends ListCell<Event> {
        private final HBox mainContainer;
        private final VBox infoContainer;
        private final ImageView imageView;
        private final Label titleLabel;
        private final Label descriptionLabel;
        private final Label dateLabel;
        private final Label statusLabel;
        private final Label userLabel;
        private final Button reserveBtn;
        private final Button editBtn;
        private final Button deleteBtn;
        private final HBox buttonBox;

        public EventListCell() {
            // Créer les composants une seule fois pour éviter les problèmes de performance
            mainContainer = new HBox(10);
            mainContainer.getStyleClass().add("event-card");
            mainContainer.setPadding(new Insets(10));
            mainContainer.setMaxWidth(Double.MAX_VALUE);

            infoContainer = new VBox(5);
            HBox.setHgrow(infoContainer, Priority.ALWAYS);

            titleLabel = new Label();
            titleLabel.getStyleClass().add("event-title");

            descriptionLabel = new Label();
            descriptionLabel.getStyleClass().add("event-description");
            descriptionLabel.setWrapText(true);

            dateLabel = new Label();
            dateLabel.getStyleClass().add("event-date");

            HBox infoBox = new HBox(10);
            statusLabel = new Label();
            userLabel = new Label();
            userLabel.getStyleClass().add("event-info");
            infoBox.getChildren().addAll(statusLabel, userLabel);

            infoContainer.getChildren().addAll(titleLabel, descriptionLabel, dateLabel, infoBox);

            VBox imageContainer = new VBox(5);
            imageContainer.setPrefWidth(150);

            imageView = new ImageView();
            imageView.setFitWidth(150);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);

            buttonBox = new HBox(5);
            buttonBox.setPadding(new Insets(5, 0, 0, 0));

            reserveBtn = new Button("Réserver");
            reserveBtn.setPrefWidth(80);
            reserveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
            reserveBtn.setOnAction(e -> {
                Event event = getItem();
                if (event != null) {
                    try {
                        reserveEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de réservation", "Une erreur est survenue: " + ex.getMessage());
                    }
                }
                e.consume(); // Empêcher la propagation de l'événement
            });

            editBtn = new Button("Modifier");
            editBtn.setPrefWidth(80);
            editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
            editBtn.setOnAction(e -> {
                Event event = getItem();
                if (event != null) {
                    try {
                        editEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", "Une erreur est survenue: " + ex.getMessage());
                    }
                }
                e.consume(); // Empêcher la propagation de l'événement
            });

            deleteBtn = new Button("Supprimer");
            deleteBtn.setPrefWidth(80);
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> {
                Event event = getItem();
                if (event != null) {
                    try {
                        deleteEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression", "Une erreur est survenue: " + ex.getMessage());
                    }
                }
                e.consume(); // Empêcher la propagation de l'événement
            });

            imageContainer.getChildren().addAll(imageView, buttonBox);
            mainContainer.getChildren().addAll(infoContainer, imageContainer);

            // Désactiver les événements de clic sur les boutons pour éviter la propagation
            reserveBtn.setOnMouseClicked(e -> e.consume());
            editBtn.setOnMouseClicked(e -> e.consume());
            deleteBtn.setOnMouseClicked(e -> e.consume());
        }

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
                mainContainer.setMaxWidth(Double.MAX_VALUE); // Permet à la cellule de s'étendre horizontalement

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
                    actionBox.setStyle("-fx-padding: 5px 0;");

                    // Bouton Réserver (pour tous les utilisateurs sauf l'organisateur)
                    if (!(isOrganiser)) {
                        final Event currentEvent = event; // Capture l'événement actuel
                        Button reserveBtn = new Button("Réserver");
                        reserveBtn.setPrefWidth(80);
                        reserveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                        reserveBtn.setOnAction(e -> {
                            try {
                                reserveEvent(currentEvent);
                                e.consume(); // Empêcher la propagation de l'événement
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'exécution", "Une erreur est survenue lors de la réservation: " + ex.getMessage());
                            }
                        });
                        reserveBtn.setOnMouseClicked(e -> e.consume()); // Empêcher la propagation du clic
                        actionBox.getChildren().add(reserveBtn);
                    }

                    // Boutons Modifier et Supprimer (pour admin et organisateur)
                    if (isAdmin || isOrganiser) {
                        final Event currentEvent = event; // Capture l'événement actuel
                        Button editBtn = new Button("Modifier");
                        editBtn.setPrefWidth(80);
                        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
                        editBtn.setOnAction(e -> {
                            try {
                                editEvent(currentEvent);
                                e.consume(); // Empêcher la propagation de l'événement
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'exécution", "Une erreur est survenue lors de la modification: " + ex.getMessage());
                            }
                        });
                        editBtn.setOnMouseClicked(e -> e.consume()); // Empêcher la propagation du clic

                        Button deleteBtn = new Button("Supprimer");
                        deleteBtn.setPrefWidth(80);
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                        deleteBtn.setOnAction(e -> {
                            try {
                                deleteEvent(currentEvent);
                                e.consume(); // Empêcher la propagation de l'événement
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'exécution", "Une erreur est survenue lors de la suppression: " + ex.getMessage());
                            }
                        });
                        deleteBtn.setOnMouseClicked(e -> e.consume()); // Empêcher la propagation du clic

                        actionBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                        // Ajouter les boutons au conteneur d'image
                    imageContainer.getChildren().add(actionBox);

                    // Empêcher la propagation des événements de clic aux cellules parentes
                    for (Node node : actionBox.getChildren()) {
                        if (node instanceof Button) {
                            node.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                setGraphic(mainContainer);
            }
        }
    }

    public void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            eventList.clear();

            // Récupérer l'utilisateur actuel
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);

            // Vider le conteneur d'événements
            eventsContainer.getChildren().clear();

            int visibleCount = 0;

            // Filtrer et ajouter chaque événement au conteneur
            for (Event event : events) {
                boolean isCreator = event.getUser() != null &&
                                   currentUser != null &&
                                   event.getUser().getId() == currentUser.getId();

                // Afficher l'événement si:
                // 1. Il est accepté (pour tous les utilisateurs)
                // 2. OU l'utilisateur est admin (voit tout)
                // 3. OU l'utilisateur est le créateur de l'événement
                if ("accepté".equals(event.getStatus()) || isAdmin || isCreator) {
                    // Créer un conteneur pour l'événement
                    HBox eventCard = createEventCard(event);
                    eventsContainer.getChildren().add(eventCard);
                    visibleCount++;

                    // Ajouter l'événement à la liste observable
                    eventList.add(event);
                }
            }

            // Mettre à jour le compteur d'événements
            totalEventsText.setText("Total: " + visibleCount + " événement(s)");
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

            // Récupérer l'utilisateur actuel
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);

            // Vider le conteneur d'événements
            eventsContainer.getChildren().clear();

            int count = 0;
            for (Event event : allEvents) {
                boolean isCreator = event.getUser() != null &&
                                   currentUser != null &&
                                   event.getUser().getId() == currentUser.getId();

                boolean matchesSearch = searchText.isEmpty() ||
                        event.getTitle().toLowerCase().contains(searchText) ||
                        event.getDescription().toLowerCase().contains(searchText);

                boolean matchesStatus = "Tous".equals(statusText) ||
                        (event.getStatus() != null && event.getStatus().equals(statusText));

                // Afficher l'événement si:
                // 1. Il correspond aux critères de recherche et de statut
                // 2. ET (il est accepté OU l'utilisateur est admin OU l'utilisateur est le créateur)
                if (matchesSearch && matchesStatus &&
                    ("accepté".equals(event.getStatus()) || isAdmin || isCreator)) {
                    // Créer un conteneur pour l'événement
                    HBox eventCard = createEventCard(event);
                    eventsContainer.getChildren().add(eventCard);
                    count++;
                }
            }

            // Mettre à jour le compteur d'événements
            totalEventsText.setText("Total: " + count + " événement(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des événements", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Créer un conteneur pour un événement
     * @param event L'événement à afficher
     * @return Un conteneur HBox contenant les informations de l'événement
     */
    private HBox createEventCard(Event event) {
        // Créer un conteneur principal pour l'affichage de l'événement
        HBox mainContainer = new HBox(10);
        mainContainer.getStyleClass().add("event-card");
        mainContainer.setPadding(new Insets(10));
        mainContainer.setMaxWidth(Double.MAX_VALUE);

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
        descriptionLabel.setWrapText(true);

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

        // Conteneur pour les boutons
        VBox buttonContainer = new VBox(5);
        buttonContainer.setPrefWidth(120);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

        // Ajouter des boutons d'action
        VBox buttonBox = new VBox(8); // Utiliser une VBox au lieu d'une HBox pour empiler les boutons verticalement
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER); // Centrer les boutons

        try {
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);
            boolean isOrganiser = event.getUser() != null &&
                    currentUser != null &&
                    event.getUser().getId() == currentUser.getId();

            // Bouton Voir détails (pour tous les utilisateurs)
            Button detailsBtn = new Button("Voir détails");
            detailsBtn.setPrefWidth(100); // Augmenter la largeur du bouton
            detailsBtn.setMinWidth(100); // Définir une largeur minimale
            detailsBtn.setMaxWidth(100); // Définir une largeur maximale
            detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            detailsBtn.setOnAction(e -> handleViewDetailsEvent(e));
            detailsBtn.setUserData(event); // Stocker l'événement dans le bouton
            buttonBox.getChildren().add(detailsBtn);

            // Bouton Avis (pour tous les utilisateurs)
            Button avisBtn = new Button("Avis");
            avisBtn.setPrefWidth(100); // Augmenter la largeur du bouton
            avisBtn.setMinWidth(100); // Définir une largeur minimale
            avisBtn.setMaxWidth(100); // Définir une largeur maximale
            avisBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            avisBtn.setOnAction(e -> handleAvisEvent(e));
            avisBtn.setUserData(event); // Stocker l'événement dans le bouton
            buttonBox.getChildren().add(avisBtn);

            // Bouton Réserver (pour tous les utilisateurs sauf l'organisateur)
            if (!(isOrganiser)) {
                Button reserveBtn = new Button("Réserver");
                reserveBtn.setPrefWidth(100); // Augmenter la largeur du bouton
                reserveBtn.setMinWidth(100); // Définir une largeur minimale
                reserveBtn.setMaxWidth(100); // Définir une largeur maximale
                reserveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                reserveBtn.setOnAction(e -> handleReserveEvent(e));
                reserveBtn.setUserData(event); // Stocker l'événement dans le bouton

                // Vérifier si l'utilisateur a déjà réservé cet événement
                try {
                    ReservationService reservationService = ReservationService.getInstance();
                    boolean hasReserved = reservationService.hasUserReservedEvent(currentUser.getId(), event.getId());

                    if (hasReserved) {
                        // Désactiver le bouton si l'utilisateur a déjà réservé
                        reserveBtn.setDisable(true);
                        reserveBtn.setText("Déjà réservé");
                        reserveBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                    }
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la vérification des réservations: " + ex.getMessage());
                    ex.printStackTrace();
                }

                buttonBox.getChildren().add(reserveBtn);
            }

            // Boutons Modifier et Supprimer (pour admin et organisateur)
            if (isAdmin || isOrganiser) {
                Button editBtn = new Button("Modifier");
                editBtn.setPrefWidth(100); // Augmenter la largeur du bouton
                editBtn.setMinWidth(100); // Définir une largeur minimale
                editBtn.setMaxWidth(100); // Définir une largeur maximale
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                editBtn.setOnAction(e -> handleEditEvent(e));
                editBtn.setUserData(event); // Stocker l'événement dans le bouton
                buttonBox.getChildren().add(editBtn);

                Button deleteBtn = new Button("Supprimer");
                deleteBtn.setPrefWidth(100); // Augmenter la largeur du bouton
                deleteBtn.setMinWidth(100); // Définir une largeur minimale
                deleteBtn.setMaxWidth(100); // Définir une largeur maximale
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                deleteBtn.setOnAction(e -> handleDeleteEvent(e));
                deleteBtn.setUserData(event); // Stocker l'événement dans le bouton
                buttonBox.getChildren().add(deleteBtn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        buttonContainer.getChildren().add(buttonBox);

        // Ajouter le conteneur d'informations et les boutons au conteneur principal
        mainContainer.getChildren().addAll(infoContainer, buttonContainer);

        // Ajouter un indicateur visuel pour les événements en attente
        if ("en attente".equals(event.getStatus())) {
            Label pendingLabel = new Label("En attente d'approbation");
            pendingLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 3;");
            infoContainer.getChildren().add(pendingLabel);
        }

        return mainContainer;
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

                // Rafraîchir les statistiques du tableau de bord
                ClientDashboardController.refreshDashboardStatistics();
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

                // Rafraîchir les statistiques du tableau de bord
                ClientDashboardController.refreshDashboardStatistics();
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
            case "accepté":
                return "status-active";
            case "rejeté":
                return "status-cancelled";
            case "en attente":
                return "status-pending";
            default:
                return "status-pending";
        }
    }

    /**
     * Réserver un événement
     * @param event L'événement à réserver
     */
    public void reserveEvent(Event event) {
        try {
            // Vérifier si l'utilisateur est connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Connexion requise", "Vous devez être connecté pour réserver un événement");
                return;
            }

            // Vérifier si l'événement est disponible
            if (!"accepté".equals(event.getStatus())) {
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
                // Rafraîchir les statistiques du tableau de bord
                ClientDashboardController.refreshDashboardStatistics();

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

                // Rafraîchir les statistiques du tableau de bord
                ClientDashboardController.refreshDashboardStatistics();

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

    public void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Gérer le clic sur le bouton "Voir détails"
     */
    @FXML
    public void handleViewDetailsEvent(ActionEvent event) {
        // Récupérer l'événement associé au bouton
        Button button = (Button) event.getSource();
        Event selectedEvent = (Event) button.getUserData();

        if (selectedEvent != null) {
            showEventDetails(selectedEvent);
        }
    }

    /**
     * Gérer le clic sur le bouton "Avis"
     */
    @FXML
    public void handleAvisEvent(ActionEvent event) {
        // Récupérer l'événement associé au bouton
        Button button = (Button) event.getSource();
        Event selectedEvent = (Event) button.getUserData();

        if (selectedEvent != null) {
            showAvis(selectedEvent);
        }
    }

    /**
     * Afficher les détails d'un événement avec la même vue que dans la partie admin
     */
    private void showEventDetails(Event event) {
        System.out.println("Voir les détails de l'événement: " + event.getTitle());

        try {
            // Créer une nouvelle fenêtre pour afficher les détails
            Stage detailStage = new Stage();
            detailStage.setTitle("Détails de l'événement: " + event.getTitle());

            // Créer le conteneur principal
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e5e9f2); -fx-background-radius: 10;");

            // Conteneur pour les informations
            VBox infoContainer = new VBox(15);
            infoContainer.setPadding(new Insets(0, 0, 0, 20));
            infoContainer.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 20;");

            // Titre de l'événement
            Label titleLabel = new Label(event.getTitle());
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Dates de l'événement
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

            // Créer un conteneur vertical pour les dates
            VBox datesContainer = new VBox(5);

            // Date de début
            Label dateDebutTitle = new Label("Date de début:");
            dateDebutTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label dateDebutLabel = new Label(dateFormat.format(event.getDate_debut()));
            dateDebutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

            // Date de fin
            Label dateFinTitle = new Label("Date de fin:");
            dateFinTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label dateFinLabel = new Label(dateFormat.format(event.getDate_fin()));
            dateFinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

            // Ajouter les labels au conteneur de dates
            datesContainer.getChildren().addAll(dateDebutTitle, dateDebutLabel, dateFinTitle, dateFinLabel);

            // Statut de l'événement
            Label statusLabel = new Label("Statut: " + event.getStatus());
            if ("accepté".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if ("rejeté".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if ("en attente".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 14px;");
            }

            // Description de l'événement
            Label descriptionTitle = new Label("Description:");
            descriptionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label descriptionLabel = new Label(event.getDescription() != null ? event.getDescription() : "Aucune description disponible");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(400);

            // Créer des séparateurs stylés
            Separator sep1 = new Separator();
            sep1.setStyle("-fx-background-color: #e0e0e0;");

            Separator sep2 = new Separator();
            sep2.setStyle("-fx-background-color: #e0e0e0;");

            // Ajouter tous les éléments au conteneur d'informations
            infoContainer.getChildren().addAll(
                titleLabel,
                datesContainer,
                statusLabel,
                sep1,
                descriptionTitle,
                descriptionLabel
            );

            // Créer un conteneur pour l'image avec un cadre et des effets
            VBox imageContainer = new VBox();
            imageContainer.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-padding: 10px; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            imageContainer.setAlignment(Pos.CENTER);
            // Permettre au conteneur d'image de s'étendre sur toute la largeur
            imageContainer.setMinWidth(800);
            imageContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
            imageContainer.setMaxWidth(Double.MAX_VALUE);
            imageContainer.setPadding(new Insets(10));

            // Essayer de charger l'image de l'événement
            ImageView imageView = new ImageView();
            imageView.setFitWidth(780); // Presque toute la largeur du conteneur
            imageView.setFitHeight(400); // Hauteur plus importante
            imageView.setPreserveRatio(true);

            // Utiliser une image par défaut générée programmatiquement
            Rectangle placeholder = new Rectangle(780, 400);
            placeholder.setFill(Color.web("#3498db"));

            // Créer un texte pour l'image par défaut
            Text placeholderText = new Text(event.getTitle());
            placeholderText.setFill(Color.WHITE);
            placeholderText.setFont(Font.font("System", FontWeight.BOLD, 24));
            placeholderText.setTextAlignment(TextAlignment.CENTER);
            placeholderText.setWrappingWidth(760);

            // Créer un StackPane pour combiner le rectangle et le texte
            StackPane placeholderImage = new StackPane(placeholder, placeholderText);

            try {
                // Si l'événement a une image, essayer de la charger
                if (event.getImage() != null && !event.getImage().isEmpty()) {
                    try {
                        // Construire le chemin correct vers l'image
                        String imagePath = event.getImage();

                        // Vérifier si le chemin commence déjà par "/images/events/"
                        if (!imagePath.startsWith("/images/events/")) {
                            // Si le chemin est juste le nom du fichier, ajouter le préfixe
                            if (!imagePath.startsWith("/")) {
                                imagePath = "/images/events/" + imagePath;
                            }
                        }

                        // Obtenir l'URL de l'image depuis les ressources
                        URL imageUrl = getClass().getResource(imagePath);

                        if (imageUrl != null) {
                            // Charger l'image depuis l'URL
                            Image image = new Image(imageUrl.toExternalForm());
                            imageView.setImage(image);
                            System.out.println("Image chargée avec succès: " + imageUrl);
                        } else {
                            // Essayer avec le chemin absolu
                            try {
                                Image image = new Image("file:" + System.getProperty("user.dir") + "/src/main/resources" + imagePath);
                                if (!image.isError()) {
                                    imageView.setImage(image);
                                    System.out.println("Image chargée avec succès depuis le chemin absolu");
                                } else {
                                    throw new Exception("Impossible de charger l'image depuis le chemin absolu");
                                }
                            } catch (Exception ex) {
                                System.err.println("Erreur lors du chargement de l'image depuis le chemin absolu: " + ex.getMessage());
                                imageView = null;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                        imageView = null;
                    }
                } else {
                    // Aucune image spécifiée, utiliser l'image par défaut générée
                    imageView = null;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement de l'image: " + e.getMessage());
                imageView = null;
            }

            // Ajouter soit l'image chargée, soit l'image par défaut générée
            if (imageView != null && imageView.getImage() != null) {
                imageContainer.getChildren().add(imageView);
            } else {
                imageContainer.getChildren().add(placeholderImage);
            }

            // Créer un conteneur principal vertical pour organiser l'affichage
            VBox mainContainer = new VBox(30); // Augmenter l'espacement entre l'image et les informations
            mainContainer.getChildren().addAll(imageContainer, infoContainer);
            mainContainer.setPadding(new Insets(0, 0, 20, 0)); // Ajouter un peu d'espace en bas

            // Ajouter le conteneur principal au centre
            root.setCenter(mainContainer);

            // Boutons d'action en bas
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(20, 0, 0, 0));

            // Bouton de fermeture
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
            closeButton.setOnAction(e -> detailStage.close());

            // Ajouter un effet hover au bouton de fermeture
            closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
            closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));

            // Ajouter le bouton de fermeture
            buttonBox.getChildren().add(closeButton);

            // Ajouter les boutons en bas
            root.setBottom(buttonBox);

            // Configurer et afficher la fenêtre
            Scene scene = new Scene(root, 900, 800); // Augmenter la hauteur pour accommoder l'image plus grande

            detailStage.setScene(scene);
            detailStage.setMinWidth(900);
            detailStage.setMinHeight(800); // Augmenter la hauteur minimale
            detailStage.show();

            // Centrer la fenêtre sur l'écran
            detailStage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des détails de l'événement: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage", "Une erreur est survenue lors de l'affichage des détails de l'événement.");
        }
    }

    /**
     * Afficher les avis pour un événement
     */
    private void showAvis(Event event) {
        try {
            // Charger la vue des avis
            File file = new File("src/main/resources/fxml/event/AvisView.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
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
                stage.initOwner(eventsContainer.getScene().getWindow());

                // Afficher la fenêtre
                stage.showAndWait();

                // Recharger les événements après la fermeture de la fenêtre
                // pour refléter les changements éventuels
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML non trouvé", file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la vue des avis", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une boîte de dialogue avec la liste des jours fériés
     */
    @FXML
    public void handleShowHolidays() {
        try {
            // Créer une boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Jours fériés");
            dialog.setHeaderText("Liste des jours fériés à venir");

            // Personnaliser le style de la boîte de dialogue
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialogPane.getStyleClass().add("holiday-dialog");

            // Créer le contenu de la boîte de dialogue
            VBox content = new VBox(15);
            content.getStyleClass().add("holiday-list");

            // Ajouter un texte explicatif
            TextFlow explanation = new TextFlow();
            Text explText = new Text("La validation des événements ne sera pas traitée pendant les weekends et les jours fériés suivants :");
            explText.getStyleClass().add("holiday-text");
            explanation.getChildren().add(explText);
            content.getChildren().add(explanation);

            // Récupérer la liste des jours fériés
            List<String> holidays = holidayService.getHolidays();

            // Créer un conteneur défilable pour les jours fériés
            VBox holidayItems = new VBox(5);
            holidayItems.getStyleClass().add("holiday-items");

            // Ajouter chaque jour férié à la liste
            for (String holiday : holidays) {
                HBox item = new HBox(10);
                item.getStyleClass().add("holiday-item");

                // Séparer le nom et la date
                String[] parts = holiday.split(" : ");
                if (parts.length == 2) {
                    Label nameLabel = new Label(parts[0]);
                    nameLabel.getStyleClass().add("holiday-name");

                    Label dateLabel = new Label(parts[1]);
                    dateLabel.getStyleClass().add("holiday-date");

                    item.getChildren().addAll(dateLabel, nameLabel);
                    holidayItems.getChildren().add(item);
                }
            }

            // Ajouter une note sur les weekends
            HBox weekendItem = new HBox(10);
            weekendItem.getStyleClass().add("holiday-item");

            Label weekendLabel = new Label("Tous les samedis et dimanches");
            weekendLabel.getStyleClass().add("holiday-name");
            weekendLabel.setStyle("-fx-font-style: italic;");

            weekendItem.getChildren().add(weekendLabel);
            holidayItems.getChildren().add(weekendItem);

            // Créer un ScrollPane pour permettre le défilement si la liste est longue
            ScrollPane scrollPane = new ScrollPane(holidayItems);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);
            scrollPane.getStyleClass().add("holiday-scroll");

            content.getChildren().add(scrollPane);

            // Définir le contenu de la boîte de dialogue
            dialogPane.setContent(content);

            // Ajouter un bouton de fermeture
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            // Afficher la boîte de dialogue
            dialog.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des jours fériés", e.getMessage());
            e.printStackTrace();
        }
    }
}
