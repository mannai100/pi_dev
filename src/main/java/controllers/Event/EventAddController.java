package controllers.Event;

import entities.Event;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import services.AuthService;
import services.EventService;
import utils.EventValidator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

public class EventAddController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private Spinner<Integer> heureDebutSpinner;

    @FXML
    private Spinner<Integer> minuteDebutSpinner;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private Spinner<Integer> heureFinSpinner;

    @FXML
    private Spinner<Integer> minuteFinSpinner;

    @FXML
    private Spinner<Integer> maxParticipantsSpinner;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField imageField;

    @FXML
    private Button browseButton;

    @FXML
    private ImageView imagePreview;

    private File selectedImageFile;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private EventService eventService;
    private AuthService authService;

    public EventAddController() {
        eventService = EventService.getInstance();
        authService = AuthService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les spinners pour les heures et minutes
        SpinnerValueFactory<Integer> heureDebutValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8);
        heureDebutSpinner.setValueFactory(heureDebutValueFactory);

        SpinnerValueFactory<Integer> minuteDebutValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteDebutSpinner.setValueFactory(minuteDebutValueFactory);

        SpinnerValueFactory<Integer> heureFinValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18);
        heureFinSpinner.setValueFactory(heureFinValueFactory);

        SpinnerValueFactory<Integer> minuteFinValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteFinSpinner.setValueFactory(minuteFinValueFactory);

        // Initialiser le spinner pour le nombre maximum de participants
        SpinnerValueFactory<Integer> maxParticipantsValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        maxParticipantsSpinner.setValueFactory(maxParticipantsValueFactory);

        // Initialiser le combobox pour le statut
        statusComboBox.getItems().addAll("actif", "annulé", "complet");
        statusComboBox.setValue("actif");

        // Initialiser les date pickers
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now());

        // Désactiver le champ de texte pour l'image (lecture seule)
        imageField.setEditable(false);
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {  // Renommé de 'event' à 'actionEvent'
        // Récupérer les valeurs des champs
        StringBuilder validationErrors = new StringBuilder();

        // Valider le titre
        String title = titleField.getText();
        String titleError = EventValidator.isValidTitle(title);
        if (titleError != null) {
            validationErrors.append("- ").append(titleError).append("\n");
        }

        // Valider la description
        String description = descriptionArea.getText();
        String descriptionError = EventValidator.isValidDescription(description);
        if (descriptionError != null) {
            validationErrors.append("- ").append(descriptionError).append("\n");
        }

        // Valider la date de début
        LocalDate dateDebut = dateDebutPicker.getValue();
        if (dateDebut == null) {
            validationErrors.append("- La date de début est obligatoire\n");
            return; // Impossible de continuer sans date de début
        }

        // Valider la date de fin
        LocalDate dateFin = dateFinPicker.getValue();
        if (dateFin == null) {
            validationErrors.append("- La date de fin est obligatoire\n");
            return; // Impossible de continuer sans date de fin
        }

        // Valider l'image
        if (selectedImageFile == null) {
            validationErrors.append("- Veuillez sélectionner une image pour l'événement\n");
        }

        // Afficher les erreurs de base si présentes
        if (validationErrors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", validationErrors.toString());
            return;
        }

        // Récupérer les heures et minutes
        int heureDebut = heureDebutSpinner.getValue();
        int minuteDebut = minuteDebutSpinner.getValue();
        LocalDateTime dateTimeDebut = LocalDateTime.of(dateDebut, LocalTime.of(heureDebut, minuteDebut));
        Date dateDebutJava = Date.from(dateTimeDebut.atZone(ZoneId.systemDefault()).toInstant());

        int heureFin = heureFinSpinner.getValue();
        int minuteFin = minuteFinSpinner.getValue();
        LocalDateTime dateTimeFin = LocalDateTime.of(dateFin, LocalTime.of(heureFin, minuteFin));
        Date dateFinJava = Date.from(dateTimeFin.atZone(ZoneId.systemDefault()).toInstant());

        // Valider les dates avec les nouvelles fonctions
        String dateDebutError = EventValidator.isValidDateDebut(dateDebutJava);
        if (dateDebutError != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "- " + dateDebutError);
            return;
        }

        String dateFinError = EventValidator.isValidDateFin(dateFinJava, dateDebutJava);
        if (dateFinError != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "- " + dateFinError);
            return;
        }
        /**
        int maxParticipants = maxParticipantsSpinner.getValue();
        String maxParticipantsError = EventValidator.isValidMaxParticipants(maxParticipants);
        if (maxParticipantsError != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "- " + maxParticipantsError);
            return;
        }
        */

        String status = statusComboBox.getValue();
        String statusError = EventValidator.isValidStatus(status);
        if (statusError != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "- " + statusError);
            return;
        }

        try {
            // Récupérer l'utilisateur connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour créer un événement");
                return;
            }

            // Créer l'événement
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setDate_debut(dateDebutJava);
            event.setDate_fin(dateFinJava);
            event.setStatus(status);
            // L'image sera définie après le téléchargement
            event.setUser(currentUser);

            // L'événement a déjà été validé par les méthodes individuelles

            // Copier l'image dans le dossier des images
            String imagePath = saveImageToServer(selectedImageFile);
            if (imagePath == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement de l'image");
                return;
            }

            // Mettre à jour le chemin de l'image dans l'objet Event
            event.setImage(imagePath);

            // Enregistrer l'événement
            eventService.addEvent(event);

            // Fermer la fenêtre
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été créé avec succès");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de l'événement: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        // Ouvrir le sélecteur de fichiers
        File selectedFile = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (selectedFile != null) {
            selectedImageFile = selectedFile;
            imageField.setText(selectedFile.getName());

            // Afficher un aperçu de l'image
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'aperçu de l'image: " + e.getMessage());
            }
        }
    }

    /**
     * Enregistre l'image sélectionnée sur le serveur
     * @param imageFile Le fichier image à enregistrer
     * @return Le chemin relatif de l'image enregistrée, ou null en cas d'erreur
     */
    private String saveImageToServer(File imageFile) {
        if (imageFile == null) {
            return null;
        }

        try {
            // Créer le dossier d'images s'il n'existe pas
            Path uploadDir = Paths.get("src/main/resources/images/events");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Générer un nom de fichier unique
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getName();
            Path destination = uploadDir.resolve(fileName);

            // Copier le fichier
            Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif
            return "/images/events/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
