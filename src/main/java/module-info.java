module org.example.workshopjdbc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.base;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires aerogear.otp.java;

    opens tests to javafx.fxml, javafx.graphics, javafx.base;
    opens controllers to javafx.fxml, javafx.graphics, javafx.base;
    opens entities to javafx.base, javafx.fxml;

    exports tests;
    exports controllers;
    exports entities;
}
