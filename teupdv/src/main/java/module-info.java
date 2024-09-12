module com.garciasolutions.teupdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.json;
    requires org.flywaydb.core;
    requires io;
    requires kernel;
    requires layout;

    opens com.garciasolutions.teupdv.models.controller to javafx.fxml;
    opens com.garciasolutions.teupdv.models.view to javafx.graphics;
    opens com.garciasolutions.teupdv.models.entities to javafx.base;

    exports com.garciasolutions.teupdv.models.view;
}