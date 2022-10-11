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
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MESSAGE_EMBED_LINKS, Permission.VIEW_CHANNEL);

        return perms;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        assert subcommandName != null;

        if (subcommandName.equals("channel")) {
            OptionMapping channelOption = event.getOption("channel");

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement statement1 = connection.prepareStatement("UPDATE logs SET channelID = ? WHERE guildID = ?")) {
                        statement1.setString(1, channelOption.getAsChannel().getId());
                        statement1.setString(2, guild.getId());

                        statement1.executeUpdate();

                        event.reply(Language.getString("LOGS_CHANNEL_SUCCESS", guild).replace("%CHANNELMENTION%", channelOption.getAsChannel().getAsMention())).setEphemeral(true).queue();
                    }
                } else {
                    try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO logs (guildID, channelID) VALUES (?,?)")) {
                        statement1.setString(1, guild.getId());
                        statement1.setString(2, channelOption.getAsChannel().getId());

                        statement1.executeUpdate();

                        event.reply(Language.getString("LOGS_CHANNEL_SUCCESS", guild).replace("%CHANNELMENTION%", channelOption.getAsChannel().getAsMention())).setEphemeral(true).queue();
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (subcommandName.equals("events")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("LOGS_EVENTS_NOCHANNEL", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                event.reply(Language.getString("LOGS_EVENTS_REPLY", guild)).addActionRow(
                        SelectMenu.create("selectMenu:logs:categoryEvents")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_CHANNEL", guild), "selectMenu:logs:categoryEvents:channel")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_EMOJI", guild), "selectMenu:logs:categoryEvents:emoji")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_BAN", guild), "selectMenu:logs:categoryEvents:ban")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_MEMBER", guild), "selectMenu:logs:categoryEvents:guildmember")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_VOICE", guild), "selectMenu:logs:categoryEvents:voice")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_MESSAGE", guild), "selectMenu:logs:categoryEvents:message")
                                .addOption(Language.getString("LOGS_EVENTS_OPTIONS_ROLE", guild), "selectMenu:logs:categoryEvents:role")
                                .setPlaceholder(Language.getString("LOGS_EVENTS_OPTIONS_PLACEHOLDER", guild))
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
