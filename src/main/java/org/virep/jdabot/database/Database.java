package org.virep.jdabot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class Database {
    static HikariDataSource dataSource;

    private final static Logger log = LoggerFactory.getLogger(Database.class);

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
            hikariConfig.addDataSourceProperty("useUnicode", "true");
            hikariConfig.addDataSourceProperty("characterEncoding", "utf8");

            log.info(String.format("Connected to HikariCP ! Pool size: %d", hikariConfig.getMaximumPoolSize()));

            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception ex) {
            ErrorManager.handleNoEvent(ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
