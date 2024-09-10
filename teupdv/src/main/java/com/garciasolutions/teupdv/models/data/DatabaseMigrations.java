package com.garciasolutions.teupdv.models.data;

import org.flywaydb.core.Flyway;

public class DatabaseMigrations {

    private static final String DATABASE_URL = "jdbc:sqlite:dbgs_restaurante.db";

    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(DATABASE_URL, null, null)
                .load();
        flyway.migrate();
    }
}

