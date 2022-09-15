package org.virep.jdabot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.virep.jdabot.utils.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            hikariConfig.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() + 1);
            hikariConfig.setMinimumIdle(hikariConfig.getMaximumPoolSize());

            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
            hikariConfig.addDataSourceProperty("useUnicode","true");
            hikariConfig.addDataSourceProperty("characterEncoding","utf8");

            System.out.printf("Pool size %d", hikariConfig.getMaximumPoolSize());

            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
