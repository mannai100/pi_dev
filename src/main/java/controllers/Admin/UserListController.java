package controllers.Admin;

import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML
    private ListView<String> userListView;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de la liste des utilisateurs
        System.out.println("Liste des utilisateurs initialisée");

        // Créer une liste d'exemple
        ObservableList<String> users = FXCollections.observableArrayList(
            "Admin 1 - admin@example.com - Administrateur",
            "User 1 - user1@example.com - Utilisateur",
            "User 2 - user2@example.com - Utilisateur",
            "User 3 - user3@example.com - Utilisateur"
        );

        // Configurer la ListView
        userListView.setItems(users);

        // Personnaliser l'affichage des cellules si nécessaire
        userListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        // Rafraîchir la liste des utilisateurs
        System.out.println("Rafraîchissement de la liste des utilisateurs");

        // Simuler un rafraîchissement en ajoutant un nouvel utilisateur
        ObservableList<String> currentUsers = userListView.getItems();
        currentUsers.add("Nouvel utilisateur - new@example.com - Utilisateur");
        userListView.setItems(currentUsers);
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

            Stage stage = (Stage) userListView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Panneau d'administration");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
