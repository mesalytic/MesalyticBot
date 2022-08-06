package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;

public class LogsCommand implements Command {
    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Lets you configure the logging system.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("modules", "desc modules"),
                        new SubcommandData("channel", "Configure the log channel.")
                                .addOption(OptionType.CHANNEL, "channel", "The log channel", true)
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

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

                event.reply("Using this menu you can select what type of events you want to log.").addActionRow(
                        SelectMenu.create("selectMenu:logs:categoryModule")
                                .addOption("Channel Events", "selectMenu:logs:categoryModule:channel")
                                .addOption("Emoji Events", "selectMenu:logs:categoryModule:emoji")
                                .setPlaceholder("Use this selection menu to toggle specific logging modules.")
                                .setMinValues(1)
                                .build()
                ).queue();

            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    }
}
