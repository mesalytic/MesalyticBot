package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

public class AfkCommand implements Command {
    @Override
    public String getName() {
        return "afk";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Let people know you're AFK when they ping you.")
                .addSubcommands(
                        new SubcommandData("set", "Set your AFK status.")
                                .addOption(OptionType.STRING, "message", "Your AFK message.", true),
                        new SubcommandData("remove", "Remove your AFK status.")
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getSubcommandName().equals("set")) {
            String message = event.getOption("message", OptionMapping::getAsString);

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE afk SET message = ? WHERE userID = ?")) {
                        updateStatement.setString(1, message);
                        updateStatement.setString(2, event.getUser().getId());

                        updateStatement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO afk (userID, message) VALUES (?,?)")) {
                        insertStatement.setString(1, event.getUser().getId());
                        insertStatement.setString(2, message);

                        insertStatement.executeUpdate();
                    }
                }

                event.reply("Your AFK status has been updated to : " + message).setAllowedMentions(Collections.emptyList()).queue();
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (event.getSubcommandName().equals("remove")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You currently do not have an AFK status set up.").setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                try (PreparedStatement removeStatement = connection.prepareStatement("DELETE FROM afk WHERE userID = ?")) {
                    removeStatement.setString(1, event.getUser().getId());

                    statement.executeUpdate();
                    event.reply("Your AFK status has been removed.").queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
