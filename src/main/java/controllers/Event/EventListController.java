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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import services.AuthService;
import services.EventService;
import services.ReservationService;
import services.RoleService;
import controllers.ClientDashboardController;

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
        // Initialiser le filtre de statut
        statusFilter.getItems().addAll("Tous", "actif", "accepté", "annulé", "complet");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(event -> filterEvents());

        // Configurer le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents());

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
                // 1. Il est actif ou accepté (pour tous les utilisateurs)
                // 2. OU l'utilisateur est admin (voit tout)
                // 3. OU l'utilisateur est le créateur de l'événement
                if ("actif".equals(event.getStatus()) || "accepté".equals(event.getStatus()) || isAdmin || isCreator) {
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
                // 2. ET (il est actif ou accepté OU l'utilisateur est admin OU l'utilisateur est le créateur)
                if (matchesSearch && matchesStatus &&
                    ("actif".equals(event.getStatus()) || "accepté".equals(event.getStatus()) || isAdmin || isCreator)) {
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

        // Ajouter des boutons d'action
        HBox buttonBox = new HBox(5);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        try {
            User currentUser = authService.getCurrentUser();
            boolean isAdmin = roleService.isAdmin(currentUser);
            boolean isOrganiser = event.getUser() != null &&
                    currentUser != null &&
                    event.getUser().getId() == currentUser.getId();

            // Bouton Réserver (pour tous les utilisateurs sauf l'organisateur)
            if (!(isOrganiser)) {
                Button reserveBtn = new Button("Réserver");
                reserveBtn.setPrefWidth(80);
                reserveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                reserveBtn.setOnAction(e -> handleReserveEvent(e));
                reserveBtn.setUserData(event); // Stocker l'événement dans le bouton
                buttonBox.getChildren().add(reserveBtn);
            }

            // Boutons Modifier et Supprimer (pour admin et organisateur)
            if (isAdmin || isOrganiser) {
                Button editBtn = new Button("Modifier");
                editBtn.setPrefWidth(80);
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
                editBtn.setOnAction(e -> handleEditEvent(e));
                editBtn.setUserData(event); // Stocker l'événement dans le bouton

                Button deleteBtn = new Button("Supprimer");
                deleteBtn.setPrefWidth(80);
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> handleDeleteEvent(e));
                deleteBtn.setUserData(event); // Stocker l'événement dans le bouton

                buttonBox.getChildren().addAll(editBtn, deleteBtn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        imageContainer.getChildren().add(buttonBox);

        // Ajouter le conteneur d'informations et l'image au conteneur principal
        mainContainer.getChildren().addAll(infoContainer, imageContainer);

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
            case "actif":
                return "status-active";
            case "accepté":
                return "status-active"; // Utiliser le même style que "actif"
            case "annulé":
                return "status-cancelled";
            case "complet":
                return "status-completed";
            case "en attente":
                return "status-pending";
            case "rejeté":
                return "status-cancelled"; // Utiliser le même style que "annulé"
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
            if (!"actif".equals(event.getStatus()) && !"accepté".equals(event.getStatus())) {
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
}
