package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogsCommand implements Command {
    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Lets you configure the logging system.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("events", "Select the events you want to be logged."),
                        new SubcommandData("channel", "Configure the log channel.")
                                .addOption(OptionType.CHANNEL, "channel", "The log channel", true)
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        assert subcommandName != null;

        Guild guild = event.getGuild();
        assert guild != null;

        if (subcommandName.equals("channel")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {

                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                OptionMapping channelOption = event.getOption("channel");
                assert channelOption != null;

                if (result.first()) {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("UPDATE logs SET channelID = ? WHERE guildID = ?")) {
                        updateStatement.setString(1, channelOption.getAsChannel().getId());
                        updateStatement.setString(2, event.getGuild().getId());

                        updateStatement.executeUpdate();

                        event.reply("Successfully set " + channelOption.getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("INSERT INTO logs (guildID, channelID) VALUES (?,?)")) {
                        updateStatement.setString(1, event.getGuild().getId());
                        updateStatement.setString(2, channelOption.getAsChannel().getId());

                        updateStatement.executeUpdate();

                        event.reply("Successfully set " + channelOption.getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (subcommandName.equals("events")) {

            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You must set up a log channel before. Use `/logs channel`").setEphemeral(true).queue();
                    return;
                }

                event.reply("Using this menu you can select what type of events you want to log.").addActionRow(
                        SelectMenu.create("selectMenu:logs:categoryEvents")
                                .addOption("Channel Related Events", "selectMenu:logs:categoryEvents:channel")
                                .addOption("Emoji Related Events", "selectMenu:logs:categoryEvents:emoji")
                                .addOption("Ban Related Events", "selectMenu:logs:categoryEvents:ban")
                                .addOption("Member Related Events", "selectMenu:logs:categoryEvents:guildmember")
                                .addOption("Voice Related Events", "selectMenu:logs:categoryEvents:voice")
                                .addOption("Message Related Events", "selectMenu:logs:categoryEvents:message")
                                .addOption("Role Related Events", "selectMenu:logs:categoryEvents:role")
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
