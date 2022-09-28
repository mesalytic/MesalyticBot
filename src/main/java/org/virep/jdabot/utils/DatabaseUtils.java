package org.virep.jdabot.utils;

import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.Main;
import org.virep.jdabot.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public String getTwitterWebhookURL(String guildID, String twitterName) {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM twitternotifier WHERE guildID = ? AND twitterAccount = ?")) {
            statement.setString(1, guildID);
            statement.setString(2, twitterName);

            ResultSet result = statement.executeQuery();

            String webhookURL = null;

            if (result.first()) {
                webhookURL = result.getString("webhookURL");
            }

            connection.close();
            return webhookURL;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return null;
        }
    }

    public static List<String> getTwitterWebhookByName(String twitterName) {
        ArrayList<String> webhooksList = new ArrayList<>();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM twitternotifier WHERE twitterAccount = ?")) {
            statement.setString(1, twitterName);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                webhooksList.add(result.getString("webhookURL"));
            }
            connection.close();
            return webhooksList;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return Collections.emptyList();
        }
    }

    public static List<String> getAllTwitterNames() {
        ArrayList<String> userNames = new ArrayList<>();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM twitternotifier")) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                userNames.add(result.getString("twitterAccount"));
            }

            connection.close();

            return userNames;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return Collections.emptyList();
        }
    }

    public static List<String> getAllTwitterNames(String guildID) {
        ArrayList<String> userNames = new ArrayList<>();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM twitternotifier WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                userNames.add(result.getString("twitterAccount"));
            }

            connection.close();

            return userNames;
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return Collections.emptyList();
        }
    }

    public static void addTwitterWebhook(String channelID, String guildID, String webhookURL, String twitterName) {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO twitternotifier (channelID, guildID, twitterAccount, webhookURL) VALUES (?,?,?,?)")) {
            statement.setString(1, channelID);
            statement.setString(2, guildID);
            statement.setString(3, twitterName);
            statement.setString(4, webhookURL);

            statement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }

    public static void removeTwitterWebhook(String guildID, String twitterName) {
        Guild guild = Main.jda.getGuildById(guildID);


        guild.retrieveWebhooks().queue(webhooks -> {
            webhooks.stream().filter(webhook -> webhook.getName().contains(twitterName)).forEach(webhook -> {
                webhook.delete().queue();
            });
        });

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM twitternotifier WHERE guildID = ? AND twitterAccount = ?")) {
            statement.setString(1, guildID);
            statement.setString(2, twitterName);

            statement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
