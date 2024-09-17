module com.garciasolutions.teupdv {
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.json;
    requires org.flywaydb.core;
    requires itextpdf;
    requires javafx.web;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires javafx.swing;

    opens com.garciasolutions.teupdv.models.controller to javafx.fxml;
    opens com.garciasolutions.teupdv.models.view to javafx.graphics;
    opens com.garciasolutions.teupdv.models.entities to javafx.base;

    exports com.garciasolutions.teupdv.models.controller;
    exports com.garciasolutions.teupdv.models.entities;
    exports com.garciasolutions.teupdv.models.view;

}
