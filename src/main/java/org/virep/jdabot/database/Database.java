package org.virep.jdabot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.virep.jdabot.utils.Config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class Database {
    static HikariDataSource dataSource;

    public static void initializeDataSource() {
        try {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
            hikariConfig.setJdbcUrl(Config.get("MARIADBHOST"));
            hikariConfig.setUsername("root");
            hikariConfig.setPassword(Config.get("MARIADBPWD"));

            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String query) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            return rs;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
