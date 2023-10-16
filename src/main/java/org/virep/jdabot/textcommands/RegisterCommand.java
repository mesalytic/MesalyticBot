package org.virep.jdabot.textcommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RegisterCommand implements SlashCommand {
    @Override
    public String getName() {
        return "register";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Register and start your Melyca Adventure!");
    }

    @Override
    public List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
            statement.setString(1, event.getUser().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                event.reply(Language.getString("REGISTER_ALREADY_REGISTERED", guild)).queue();

                return;
            }

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO inventory (userID) VALUES (?)");
            insertStatement.setString(1, event.getUser().getId());

            insertStatement.executeUpdate();

            event.reply(Language.getString("REGISTER_REGISTERED", guild)).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
