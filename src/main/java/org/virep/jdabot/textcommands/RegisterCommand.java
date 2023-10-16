package org.virep.jdabot.textcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.handlers.TextCommand;
import org.virep.jdabot.language.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterCommand implements TextCommand {
    @Override
    public String getName() {
        return "register";
    }
    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws SQLException {
        Guild guild = event.getGuild();
        Message message = event.getMessage();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
            statement.setString(1, event.getAuthor().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                message.reply(Language.getString("REGISTER_ALREADY_REGISTERED", guild)).queue();

                return;
            }

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO inventory (userID) VALUES (?)");
            insertStatement.setString(1, event.getAuthor().getId());

            insertStatement.executeUpdate();

            message.reply(Language.getString("REGISTER_REGISTERED", guild)).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
