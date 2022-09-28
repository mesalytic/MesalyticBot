package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

public class AfkListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
            statement.setString(1, event.getAuthor().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM afk WHERE userID = ?")) {
                    deleteStatement.setString(1, event.getAuthor().getId());

                    deleteStatement.executeUpdate();

                    event.getMessage().reply(event.getAuthor().getAsMention() + ", your AFK status has been removed.").queue();
                }
            }
            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }

        if (event.getMessage().getMentions().getUsers().isEmpty()) return;
        User mentionedUser = event.getMessage().getMentions().getUsers().get(0);

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
            statement.setString(1, mentionedUser.getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                event.getMessage().reply("**" + mentionedUser.getAsTag() + "** is AFK right now. Reason: " + result.getString("message")).setAllowedMentions(Collections.emptyList()).queue();
            }
            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
