package org.virep.jdabot.utils;

import org.virep.jdabot.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DatabaseUtils {
    public static boolean isEnabled(String logType, String guildID) {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();

            boolean isEnabled = false;

            if (result.first()) {
                int typeIndex = result.findColumn(logType);
                isEnabled = Boolean.parseBoolean(result.getString(typeIndex));
            }

            connection.close();
            return isEnabled;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return false;
        }
    }

    public static String getLogChannelID(String guildID) {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();
            ResultSetMetaData resultSetMetaData = result.getMetaData();

            String channelID = null;

            if (result.first()) {
                channelID = result.getString(resultSetMetaData.getColumnCount());

                connection.close();
            }
            return channelID;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return null;
        }
    }

}
