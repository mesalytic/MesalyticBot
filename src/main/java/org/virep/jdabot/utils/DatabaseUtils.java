package org.virep.jdabot.utils;

import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public String getTwitterWebhookURL(String guildID, String twitterName) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM twitternotifier WHERE guildID = ? AND twitterAccount = ?")) {
            statement.setString(1, guildID);
            statement.setString(2, twitterName);

            ResultSet result = statement.executeQuery();

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

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM twitternotifier WHERE twitterAccount = ?")) {
            statement.setString(1, twitterName);

            ResultSet result = statement.executeQuery();

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

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM twitternotifier")) {
            ResultSet result = statement.executeQuery();

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

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM twitternotifier WHERE guildID = ?")) {
            statement.setString(1, guildID);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                userNames.add(result.getString("twitterAccount"));
            }

            return userNames;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addTwitterWebhook(String channelID, String guildID, String webhookURL, String twitterName) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("INSERT INTO twitternotifier (channelID, guildID, twitterAccount, webhookURL) VALUES (?,?,?,?)")) {
            statement.setString(1, channelID);
            statement.setString(2, guildID);
            statement.setString(3, twitterName);
            statement.setString(4, webhookURL);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeTwitterWebhook(String guildID, String twitterName) {
        Guild guild = Main.PublicJDA.getGuildById(guildID);



        guild.retrieveWebhooks().queue(webhooks -> {
            webhooks.stream().filter(webhook -> webhook.getName().contains(twitterName)).forEach(webhook -> {
                webhook.delete().queue();
            });
        });

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("DELETE FROM twitternotifier WHERE guildID = ? AND twitterAccount = ?")) {
            statement.setString(1, guildID);
            statement.setString(2, twitterName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
