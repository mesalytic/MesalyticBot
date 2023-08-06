package org.virep.jdabot.commands.game;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RegisterCommand implements Command {
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
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
            statement.setString(1, event.getUser().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                event.reply("X - You are already registered !").queue();

                return;
            }

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO inventory (userID) VALUES (?)");
            insertStatement.setString(1, event.getUser().getId());

            insertStatement.executeUpdate();

            event.reply("V - You have been successfully registered ! Welcome to Melyca.").queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
