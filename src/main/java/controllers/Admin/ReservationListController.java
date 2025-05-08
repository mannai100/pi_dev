package controllers.Admin;

import entities.Event;
import entities.ReserverEvent;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;
import java.io.IOException;
import services.ReservationService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class ReservationListController implements Initializable {

    @FXML
    private ListView<ReserverEvent> reservationListView;

    @FXML
    private Button backButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    private ReservationService reservationService;

    private ObservableList<ReserverEvent> allReservations = FXCollections.observableArrayList();
    private FilteredList<ReserverEvent> filteredReservations;

    public ReservationListController() {
        reservationService = ReservationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer la ListView pour afficher les réservations
        reservationListView.setCellFactory(new Callback<ListView<ReserverEvent>, ListCell<ReserverEvent>>() {
            @Override
            public ListCell<ReserverEvent> call(ListView<ReserverEvent> param) {
                return new ListCell<ReserverEvent>() {
                    @Override
                    protected void updateItem(ReserverEvent reservation, boolean empty) {
                        super.updateItem(reservation, empty);
                        if (empty || reservation == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Formater l'affichage de la réservation
                            String userName = reservation.getUser() != null ?
                                    reservation.getUser().getPrenom() + " " + reservation.getUser().getNom() : "Utilisateur inconnu";
                            String eventTitle = reservation.getEvent() != null ?
                                    reservation.getEvent().getTitle() : "Événement inconnu";

                            // Créer un conteneur pour une meilleure présentation
                            VBox container = new VBox(5);
                            container.setPadding(new Insets(5, 0, 5, 0));

                            Label titleLabel = new Label("Réservation #" + reservation.getId() + " - " + eventTitle);
                            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                            Label userLabel = new Label("Réservé par: " + userName);
                            userLabel.setStyle("-fx-font-size: 12px;");

                            Label statusLabel = new Label("Statut: " + reservation.getStatut());
                            statusLabel.setStyle("-fx-font-size: 12px;");

                            // Appliquer un style différent selon le statut
                            if ("confirmé".equals(reservation.getStatut())) {
                                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            } else if ("annulé".equals(reservation.getStatut())) {
                                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            }

                            container.getChildren().addAll(titleLabel, userLabel, statusLabel);
                            setGraphic(container);
                        }
                    }
                };
            }
        });

        // Charger les données
        loadReservations();

        // Configurer la recherche en temps réel
        setupSearch();
    }



    private void loadReservations() {
        try {
            List<ReserverEvent> reservations = reservationService.getAllReservations();
            allReservations.setAll(reservations);

            // Initialiser la liste filtrée avec toutes les réservations
            filteredReservations = new FilteredList<>(allReservations, p -> true);
            reservationListView.setItems(filteredReservations);

            // Le compteur de réservations a été supprimé
        } catch (SQLException e) {
            e.printStackTrace();
            // Afficher un message d'erreur

            // En cas d'erreur, afficher des données factices pour la démonstration
            ObservableList<ReserverEvent> demoReservations = FXCollections.observableArrayList();

            // Créer une réservation factice
            ReserverEvent reservation1 = new ReserverEvent();
            reservation1.setId(1);
            reservation1.setStatut("confirmé");
            reservation1.setDateReservation(new Date());

            User user1 = new User();
            user1.setId(1);
            user1.setNom("Dupont");
            user1.setPrenom("Jean");
            reservation1.setUser(user1);

            Event event1 = new Event();
            event1.setId(1);
            event1.setTitle("Conférence JavaFX");
            reservation1.setEvent(event1);

            // Ajouter une deuxième réservation factice
            ReserverEvent reservation2 = new ReserverEvent();
            reservation2.setId(2);
            reservation2.setStatut("en attente");
            reservation2.setDateReservation(new Date());

            User user2 = new User();
            user2.setId(2);
            user2.setNom("Martin");
            user2.setPrenom("Sophie");
            reservation2.setUser(user2);

            Event event2 = new Event();
            event2.setId(2);
            event2.setTitle("Workshop UI/UX");
            reservation2.setEvent(event2);

            demoReservations.addAll(reservation1, reservation2);
            allReservations.setAll(demoReservations);

            // Initialiser la liste filtrée avec les réservations de démo
            filteredReservations = new FilteredList<>(allReservations, p -> true);
            reservationListView.setItems(filteredReservations);

            // Le compteur de réservations a été supprimé
        }
    }



    @FXML
    private void handleRefresh(ActionEvent event) {
        // Rafraîchir la liste des réservations
        loadReservations();
    }

    // Le bouton de recherche a été supprimé

    @FXML
    private void handleSearchKey(KeyEvent event) {
        // Cette méthode est appelée à chaque frappe de clavier
        // La recherche est déjà en temps réel, donc pas besoin de faire quoi que ce soit ici
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        // Ouvrir le formulaire d'ajout de réservation
        System.out.println("Ouverture du formulaire d'ajout de réservation");
    }

    private void setupSearch() {
        // Ne rien faire si le champ de recherche n'est pas disponible
        if (searchField == null) {
            System.err.println("Champ de recherche non disponible");
            return;
        }

        // Si la liste est vide, ne pas configurer la recherche
        if (allReservations == null || allReservations.isEmpty()) {
            System.out.println("Liste de réservations vide, recherche désactivée");
            return;
        }

        try {
            // Configurer l'écouteur pour le champ de recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredReservations != null) { // Vérifier que la liste filtrée existe toujours
                    filteredReservations.setPredicate(reservation -> {
                        // Si le champ de recherche est vide, afficher toutes les réservations
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }

                        // Convertir le texte de recherche en minuscules
                        String lowerCaseFilter = newValue.toLowerCase();

                        // Recherche par nom d'utilisateur
                        boolean userMatch = reservation.getUser() != null &&
                            ((reservation.getUser().getNom() != null && reservation.getUser().getNom().toLowerCase().contains(lowerCaseFilter)) ||
                             (reservation.getUser().getPrenom() != null && reservation.getUser().getPrenom().toLowerCase().contains(lowerCaseFilter)));

                        // Recherche par titre d'événement
                        boolean eventMatch = reservation.getEvent() != null &&
                            reservation.getEvent().getTitle() != null &&
                            reservation.getEvent().getTitle().toLowerCase().contains(lowerCaseFilter);

                        // Recherche par statut
                        boolean statusMatch = reservation.getStatut() != null &&
                            reservation.getStatut().toLowerCase().contains(lowerCaseFilter);

                        return userMatch || eventMatch || statusMatch;
                    });

                    // Mettre à jour la ListView
                    reservationListView.setItems(filteredReservations);

                    // Le compteur de réservations a été supprimé
                }
            });

            System.out.println("Recherche en temps réel configurée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration de la recherche: " + e.getMessage());
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

            Stage stage = (Stage) reservationListView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Panneau d'administration");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
