package org.virep.jdabot.utils;

import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.Main;
import org.virep.jdabot.database.Database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    public static boolean isEnabled(String logType, String guildID) {
        try {
            ResultSet result = Database.executeQuery("SELECT * FROM logs WHERE guildID = " + guildID);

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
        try {
            ResultSet result = Database.executeQuery("SELECT * FROM logs WHERE guildID = " + guildID);
            ResultSetMetaData resultSetMetaData = result.getMetaData();

            if (result.first()) {
                return result.getString(resultSetMetaData.getColumnCount());
            } else return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getTwitterWebhookURL(String guildID, String twitterName) {
        try {
            ResultSet result = Database.executeQuery("SELECT * FROM twitternotifier WHERE guildID = " + guildID + " AND twitterAccount = " + twitterName);

            if (result.first()) {
                return result.getString("webhookURL");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getTwitterWebhookByName(String twitterName) {
        ArrayList<String> webhooksList = new ArrayList<>();

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM twitternotifier WHERE twitterAccount = " + twitterName);

            while (result.next()) {
                webhooksList.add(result.getString("webhookURL"));
            }

            return webhooksList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllTwitterNames() {
        ArrayList<String> userNames = new ArrayList<>();

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM twitternotifier");

            while (result.next()) {
                userNames.add(result.getString("twitterAccount"));
            }

            return userNames;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllTwitterNames(String guildID) {
        ArrayList<String> userNames = new ArrayList<>();

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM twitternotifier WHERE guildID = " + guildID);

            while (result.next()) {
                userNames.add(result.getString("twitterAccount"));
            }

            return userNames;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addTwitterWebhook(String channelID, String guildID, String webhookURL, String twitterName) {
        Database.executeQuery("INSERT INTO twitternotifier (channelID, guildID, twitterAccount, webhookURL) VALUES (" + channelID + "," + guildID + "," + twitterName + "," + webhookURL + ")");
    }

    public static void removeTwitterWebhook(String guildID, String twitterName) {
        Guild guild = Main.PublicJDA.getGuildById(guildID);


        guild.retrieveWebhooks().queue(webhooks -> {
            webhooks.stream().filter(webhook -> webhook.getName().contains(twitterName)).forEach(webhook -> {
                webhook.delete().queue();
            });
        });

        Database.executeQuery("DELETE FROM twitternotifier WHERE guildID = " + guildID + " AND twitterAccount = " + twitterName);
    }
}
