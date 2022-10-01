package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
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
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le système de logs.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("events", "Select the events you want to be logged.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Selectionner les évenements que vous voulez log."),
                        new SubcommandData("channel", "Configure the log channel.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le salon de log.")
                                .addOptions(
                                        new OptionData(OptionType.CHANNEL, "channel", "The log channel", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon de log.")
                                )
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
            event.reply("\u274C - You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        assert subcommandName != null;

        Guild guild = event.getGuild();
        assert guild != null;

        if (subcommandName.equals("channel")) {
            OptionMapping channelOption = event.getOption("channel");

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement statement1 = connection.prepareStatement("UPDATE logs SET channelID = ? WHERE guildID = ?")) {
                        statement1.setString(1, channelOption.getAsChannel().getId());
                        statement1.setString(2, event.getGuild().getId());

                        statement1.executeUpdate();

                        event.reply("\u2705 - Successfully set " + channelOption.getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    }
                } else {
                    try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO logs (guildID, channelID) VALUES (?,?)")) {
                        statement1.setString(1, event.getGuild().getId());
                        statement1.setString(2, channelOption.getAsChannel().getId());

                        statement1.executeUpdate();

                        event.reply("\u2705 - Successfully set " + channelOption.getAsChannel().getAsMention() + " as the log channel for this server.").setEphemeral(true).queue();
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (subcommandName.equals("events")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("\u274C - You must set up a log channel before. Use `/logs channel`").setEphemeral(true).queue();
                    connection.close();
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

                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
