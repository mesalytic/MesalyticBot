package org.virep.jdabot.utils;

import org.virep.jdabot.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DatabaseUtils {
    public static boolean isEnabled(String logType, String guildID) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                int typeIndex = result.findColumn(logType);

                return Boolean.parseBoolean(result.getString(typeIndex));
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getLogChannelID(String guildID) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();
            ResultSetMetaData resultSetMetaData = result.getMetaData();

            if (result.first()) return result.getString(resultSetMetaData.getColumnCount());
            else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
