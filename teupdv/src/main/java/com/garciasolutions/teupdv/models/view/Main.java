package com.garciasolutions.teupdv.models.view;

import com.garciasolutions.teupdv.models.data.DatabaseMigrations;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        DatabaseMigrations.migrate();
        LoginView.main(args);
    }

    public static void launchApp(String[] args) {
        Application.launch(DashboardView.class, args);
    }
}
