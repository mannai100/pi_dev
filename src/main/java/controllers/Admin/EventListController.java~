package controllers.Admin;

import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.net.URL;
import services.EventService;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EventListController implements Initializable {

    @FXML
    private ListView<Event> eventListView;

    @FXML
    private Button backButton;

    @FXML
    private TextField searchField;

    @FXML
    private Pagination eventPagination;

    private EventService eventService;

    private ObservableList<Event> allEvents = FXCollections.observableArrayList();
    private FilteredList<Event> filteredEvents;
    private static final int ITEMS_PER_PAGE = 5;

    public EventListController() {
        eventService = EventService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer la pagination
        eventPagination.setPageFactory(this::createPage);

        // Configurer la ListView pour afficher les événements
        eventListView.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {
            @Override
            public ListCell<Event> call(ListView<Event> param) {
                return new ListCell<Event>() {
                    @Override
                    protected void updateItem(Event event, boolean empty) {
                        super.updateItem(event, empty);
                        if (empty || event == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Créer un conteneur pour une meilleure présentation
                            VBox container = new VBox(5);
                            container.setPadding(new Insets(5, 0, 5, 0));

                            // Titre de l'événement
                            Label titleLabel = new Label(event.getTitle());
                            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                            // Dates de l'événement
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String dateInfo = "Du " + dateFormat.format(event.getDate_debut()) +
                                             " au " + dateFormat.format(event.getDate_fin());
                            Label dateLabel = new Label(dateInfo);
                            dateLabel.setStyle("-fx-font-size: 12px;");

                            // Statut de l'événement
                            Label statusLabel = new Label("Statut: " + event.getStatus());

                            // Appliquer un style différent selon le statut
                            if ("actif".equals(event.getStatus()) || "accepté".equals(event.getStatus())) {
                                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            } else if ("annulé".equals(event.getStatus()) || "rejeté".equals(event.getStatus())) {
                                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            } else if ("complet".equals(event.getStatus())) {
                                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            } else if ("en attente".equals(event.getStatus())) {
                                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            }

                            // Ajouter un bouton pour voir les détails
                            HBox actionButtons = new HBox(10);
                            actionButtons.setAlignment(Pos.CENTER_RIGHT);

                            Button viewButton = new Button("Voir les détails");
                            viewButton.getStyleClass().addAll("view-button", "action-button");
                            viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                            viewButton.setOnAction(e -> handleViewEvent(event));

                            actionButtons.getChildren().add(viewButton);

                            container.getChildren().addAll(titleLabel, dateLabel, statusLabel, actionButtons);
                            setGraphic(container);
                        }
                    }
                };
            }
        });

        // Charger les données
        loadEvents();

        // Configurer la recherche en temps réel
        setupSearch();
    }

    private void handleViewEvent(Event event) {
        System.out.println("Voir l'événement: " + event.getTitle());

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
            if ("actif".equals(event.getStatus()) || "accepté".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if ("annulé".equals(event.getStatus()) || "rejeté".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if ("complet".equals(event.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 14px;");
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

            Separator sep3 = new Separator();
            sep3.setStyle("-fx-background-color: #e0e0e0;");

            // Ajouter tous les éléments au conteneur d'informations
            infoContainer.getChildren().addAll(
                titleLabel,
                datesContainer,
                statusLabel,
                sep1,
                descriptionTitle,
                descriptionLabel
            );

            // Essayer de charger l'image de l'événement
            ImageView imageView = new ImageView();
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
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

            // Créer un conteneur pour l'image avec un cadre et des effets
            VBox imageContainer = new VBox();
            imageContainer.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-padding: 10px; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            imageContainer.setAlignment(Pos.CENTER);
            // Permettre au conteneur d'image de s'étendre sur toute la largeur
            imageContainer.setMinWidth(800);
            imageContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
            imageContainer.setMaxWidth(Double.MAX_VALUE);
            imageContainer.setPadding(new Insets(10));

            // Modifier les dimensions de l'image pour l'afficher verticalement sur toute la largeur
            if (imageView != null) {
                imageView.setFitWidth(780); // Presque toute la largeur du conteneur
                imageView.setFitHeight(400); // Hauteur plus importante
                imageView.setPreserveRatio(true);
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

            // Si l'événement est en attente, ajouter les boutons d'approbation et de rejet
            if ("en attente".equals(event.getStatus())) {
                // Bouton d'approbation
                Button approveButton = new Button("Approuver");
                approveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");

                // Bouton de rejet - Définir la variable avant de l'utiliser
                Button rejectButton = new Button("Rejeter");
                rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");

                approveButton.setOnAction(e -> {
                    try {
                        // Mettre à jour le statut de l'événement
                        EventService eventService = EventService.getInstance();
                        eventService.updateEventStatus(event.getId(), EventService.STATUS_APPROVED);

                        // Mettre à jour l'affichage
                        statusLabel.setText("Statut: " + EventService.STATUS_APPROVED);
                        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

                        // Masquer les boutons d'approbation et de rejet
                        buttonBox.getChildren().removeAll(approveButton, git addrejectButton);

                        // Afficher un message de succès
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText("Événement approuvé");
                        alert.setContentText("L'événement a été approuvé avec succès.");
                        alert.showAndWait();

                        // Rafraîchir la liste des événements
                        loadEvents();

                        // Rafraîchir les statistiques du tableau de bord client
                        controllers.ClientDashboardController.refreshDashboardStatistics();
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de l'approbation de l'événement: " + ex.getMessage());
                        ex.printStackTrace();

                        // Afficher un message d'erreur
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Erreur lors de l'approbation de l'événement");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
                    }
                });

                // Ajouter un effet hover au bouton d'approbation
                approveButton.setOnMouseEntered(e -> approveButton.setStyle("-fx-background-color: #219653; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
                approveButton.setOnMouseExited(e -> approveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));

                // Configuration du bouton de rejet
                rejectButton.setOnAction(e -> {
                    try {
                        // Mettre à jour le statut de l'événement
                        EventService eventService = EventService.getInstance();
                        eventService.updateEventStatus(event.getId(), EventService.STATUS_REJECTED);

                        // Mettre à jour l'affichage
                        statusLabel.setText("Statut: " + EventService.STATUS_REJECTED);
                        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");

                        // Masquer les boutons d'approbation et de rejet
                        buttonBox.getChildren().removeAll(approveButton, rejectButton);

                        // Afficher un message de succès
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText("Événement rejeté");
                        alert.setContentText("L'événement a été rejeté avec succès.");
                        alert.showAndWait();

                        // Rafraîchir la liste des événements
                        loadEvents();

                        // Rafraîchir les statistiques du tableau de bord client
                        controllers.ClientDashboardController.refreshDashboardStatistics();
                    } catch (Exception ex) {
                        System.err.println("Erreur lors du rejet de l'événement: " + ex.getMessage());
                        ex.printStackTrace();

                        // Afficher un message d'erreur
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Erreur lors du rejet de l'événement");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
                    }
                });

                // Ajouter un effet hover au bouton de rejet
                rejectButton.setOnMouseEntered(e -> rejectButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
                rejectButton.setOnMouseExited(e -> rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));

                // Ajouter les boutons d'approbation et de rejet
                buttonBox.getChildren().addAll(approveButton, rejectButton);
            }

            // Ajouter les boutons en bas
            root.setBottom(buttonBox);

            // Configurer et afficher la fenêtre
            Scene scene = new Scene(root, 900, 800); // Augmenter la hauteur pour accommoder l'image plus grande
            scene.getStylesheets().add(getClass().getResource("/styles/admin-styles.css").toExternalForm());

            detailStage.setScene(scene);
            detailStage.setMinWidth(900);
            detailStage.setMinHeight(800); // Augmenter la hauteur minimale
            detailStage.show();

            // Centrer la fenêtre sur l'écran
            detailStage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des détails de l'événement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        try {
            URL url = getClass().getResource("/fxml/admin/AdminDashboard.fxml");
            if (url == null) {
                System.err.println("Impossible de trouver le fichier FXML: /fxml/admin/AdminDashboard.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) eventListView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Panneau d'administration");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            allEvents.setAll(events);

            // Initialiser la liste filtrée avec tous les événements
            filteredEvents = new FilteredList<>(allEvents, p -> true);

            // Configurer la pagination
            int pageCount = (filteredEvents.size() / ITEMS_PER_PAGE) + (filteredEvents.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
            eventPagination.setPageCount(Math.max(1, pageCount));
            eventPagination.setCurrentPageIndex(0);

            // Mettre à jour la ListView avec la première page
            updateListView(0);

            // Le compteur d'événements a été supprimé
        } catch (SQLException e) {
            e.printStackTrace();
            // Afficher un message d'erreur

            // En cas d'erreur, afficher des données factices pour la démonstration
            Event event1 = new Event();
            event1.setId(1);
            event1.setTitle("Conférence JavaFX");
            event1.setStatus("actif");
            // Ajouter des dates pour éviter les NullPointerException
            event1.setDate_debut(new Date());
            event1.setDate_fin(new Date());

            Event event2 = new Event();
            event2.setId(2);
            event2.setTitle("Workshop UI/UX");
            event2.setStatus("actif");
            // Ajouter des dates pour éviter les NullPointerException
            event2.setDate_debut(new Date());
            event2.setDate_fin(new Date());

            Event event3 = new Event();
            event3.setId(3);
            event3.setTitle("Hackathon IA");
            event3.setStatus("actif");
            event3.setDate_debut(new Date());
            event3.setDate_fin(new Date());

            Event event4 = new Event();
            event4.setId(4);
            event4.setTitle("Formation Spring Boot");
            event4.setStatus("actif");
            event4.setDate_debut(new Date());
            event4.setDate_fin(new Date());

            Event event5 = new Event();
            event5.setId(5);
            event5.setTitle("Séminaire DevOps");
            event5.setStatus("actif");
            event5.setDate_debut(new Date());
            event5.setDate_fin(new Date());

            Event event6 = new Event();
            event6.setId(6);
            event6.setTitle("Atelier Mobile Development");
            event6.setStatus("actif");
            event6.setDate_debut(new Date());
            event6.setDate_fin(new Date());

            ObservableList<Event> demoEvents = FXCollections.observableArrayList(event1, event2, event3, event4, event5, event6);
            allEvents.setAll(demoEvents);

            // Initialiser la liste filtrée avec les événements de démo
            filteredEvents = new FilteredList<>(allEvents, p -> true);

            // Configurer la pagination
            int pageCount = (filteredEvents.size() / ITEMS_PER_PAGE) + (filteredEvents.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
            eventPagination.setPageCount(Math.max(1, pageCount));
            eventPagination.setCurrentPageIndex(0);

            // Mettre à jour la ListView avec la première page
            updateListView(0);

            // Le compteur d'événements a été supprimé
        }
    }



    @FXML
    private void handleRefresh(ActionEvent event) {
        // Rafraîchir la liste des événements
        loadEvents();
    }

    // Le bouton de recherche a été supprimé

    @FXML
    private void handleSearchKey(KeyEvent event) {
        // Cette méthode est appelée à chaque frappe de clavier
        // La recherche est déjà en temps réel, donc pas besoin de faire quoi que ce soit ici
    }

    private void setupSearch() {
        // Ne rien faire si le champ de recherche n'est pas disponible
        if (searchField == null) {
            System.err.println("Champ de recherche non disponible");
            return;
        }

        // Si la liste est vide, ne pas configurer la recherche
        if (allEvents == null || allEvents.isEmpty()) {
            System.out.println("Liste d'événements vide, recherche désactivée");
            return;
        }

        try {
            // Configurer l'écouteur pour le champ de recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredEvents != null) { // Vérifier que la liste filtrée existe toujours
                    filteredEvents.setPredicate(event -> {
                        // Si le champ de recherche est vide, afficher tous les événements
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }

                        // Convertir le texte de recherche en minuscules
                        String lowerCaseFilter = newValue.toLowerCase();

                        // Vérifier si le texte correspond à un attribut de l'événement
                        if (event.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (event.getDescription() != null && event.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (event.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        return false;
                    });

                    // Mettre à jour la pagination
                    int pageCount = (filteredEvents.size() / ITEMS_PER_PAGE) + (filteredEvents.size() % ITEMS_PER_PAGE > 0 ? 1 : 0);
                    eventPagination.setPageCount(Math.max(1, pageCount));
                    eventPagination.setCurrentPageIndex(0);

                    // Mettre à jour la ListView
                    updateListView(0);

                    // Le compteur d'événements a été supprimé
                }
            });

            System.out.println("Recherche en temps réel configurée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Node createPage(int pageIndex) {
        // Cette méthode est appelée par la pagination pour créer chaque page
        updateListView(pageIndex);
        return eventListView;
    }

    private void updateListView(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredEvents.size());

        if (fromIndex > toIndex) {
            eventListView.setItems(FXCollections.observableArrayList());
            return;
        }

        // Créer une sous-liste pour la page actuelle
        ObservableList<Event> pageItems = FXCollections.observableArrayList(
            filteredEvents.subList(fromIndex, toIndex)
        );

        eventListView.setItems(pageItems);
    }
}
