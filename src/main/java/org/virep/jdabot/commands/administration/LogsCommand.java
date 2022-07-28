package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LogsCommand extends SlashCommand {
    public LogsCommand() {
        super("logs", "Lets you configure the logging system.", true, new SubcommandData[]{
                new SubcommandData("modules", "desc modules"),
                new SubcommandData("channel", "Configure the log channel.").addOption(OptionType.CHANNEL, "channel", "The log channel", true)
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getSubcommandName().equals("channel")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("UPDATE logs SET channelID = ? WHERE guildID = ?")) {
                        updateStatement.setString(1, event.getOption("channel").getAsChannel().getId());
                        updateStatement.setString(2, event.getGuild().getId());

                        updateStatement.executeUpdate();

                        event.reply("Successfully set " + event.getOption("channel").getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("INSERT INTO logs (guildID, channelID) VALUES (?,?)")) {
                        updateStatement.setString(1, event.getGuild().getId());
                        updateStatement.setString(2, event.getOption("channel").getAsChannel().getId());

                        updateStatement.executeUpdate();

                        event.reply("Successfully set " + event.getOption("channel").getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (event.getSubcommandName().equals("modules")) {

            HashSet<String> col = new HashSet<>();

            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You must set up a log channel before. Use `/logs channel`").setEphemeral(true).queue();
                    return;
                }

                ResultSetMetaData resultSetMetaData = result.getMetaData();

                for (int i = 1; i < resultSetMetaData.getColumnCount() - 1; i++) {
                    String modState = result.getString(i);

                    if (modState.equals("true")) col.add("selectMenu:logs:modules:" + resultSetMetaData.getColumnName(i));
                }

                event.reply("Using this menu you can select what type of events you want to log.").setEphemeral(true).addActionRow(
                        SelectMenu.create("selectMenu:logs:modules")
                                .addOption("Channel Creation", "selectMenu:logs:modules:channelCreate")
                                .addOption("Channel Deletion", "selectMenu:logs:modules:channelDelete")
                                .setPlaceholder("Use this selection menu to toggle specific logging modules.")
                                .setMaxValues(2)
                                .setMinValues(0)
                                .setDefaultValues(col)
                                .build()
                ).queue();

            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    }
}
