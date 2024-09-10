package com.garciasolutions.teupdv.models.view;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        LoginView.main(args);
    }

    public static void launchApp(String[] args) {
        Application.launch(DashboardView.class, args);
    }
}
