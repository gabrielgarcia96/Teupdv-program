package com.garciasolutions.teupdv.models.entities;

public class UserSession {
    private static UserSession instance;
    private String currentUsername;
    private AcessLevel accessLevel;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public AcessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AcessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}

