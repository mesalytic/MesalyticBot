package org.virep.jdabot.database;

import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.virep.jdabot.utils.Config;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnector {
    private static MariaDbPoolDataSource dataSource;

    private static void setupDataSource() throws SQLException {
        dataSource = new MariaDbPoolDataSource();
        dataSource.setUrl(Config.get("MARIADBHOST"));
        dataSource.setUser("root");
        dataSource.setPassword(Config.get("MARIADBPWD"));
    }

    public static Connection openConnection() {
        try {
            if (dataSource == null) {
                setupDataSource();
            }
            System.out.println("Connected DB");
            return dataSource.getConnection();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Database Connection couldn't be established.");
        }
    }
}
