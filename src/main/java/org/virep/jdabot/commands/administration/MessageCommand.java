package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageCommand implements Command {
    @Override
    public String getName() {
        return "message";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Lets you configure join and leave messages.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure les messages de bienvenue et de départs.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommandGroups(
                        new SubcommandGroupData("join", "The bot will send a message whenever someone joins the server.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le bot enverra un message lorsque quelqu'un rejoindra le serveur.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the join message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le message de bienvenue.")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon auquel le message sera envoyé."),
                                                        new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message join tags.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le message qui sera envoyé. Vous pouvez utiliser des tags (/message join tags)")
                                                ),
                                        new SubcommandData("remove", "Remove the join message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire le message de bienvenue."),
                                        new SubcommandData("tags", "List of tags you can use on your message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste des tags que vous pouvez utiliser sur votre message."),
                                        new SubcommandData("test", "Tests the join message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Test le message de bienvenue.")
                                ),
                        new SubcommandGroupData("leave", "The bot will send a message whenever someone leaves the server.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le bot enverra un message lorsque quelqu'un quittera le serveur.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the leave message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le message de départ.")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon auquel le message sera envoyé."),
                                                        new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message leave tags.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le message qui sera envoyé. Vous pouvez utiliser des tags (/message leave tags)")
                                                ),
                                        new SubcommandData("remove", "Remove the leave message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire le message de départ"),
                                        new SubcommandData("tags", "List of tags you can use on your message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste des tags que vous pouvez utiliser sur votre message."),
                                        new SubcommandData("test", "Test your message !")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Test le message de départ.")
                                ),
                        new SubcommandGroupData("dm", "The bot will send a DM whenever someone joins the server.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le bot enverra un message privé au membre qui aura rejoins le serveur.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le message privé.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message dm tags.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le message qui sera envoyé. Vous pouvez utiliser des tags (/message dm tags)")
                                                ),
                                        new SubcommandData("remove", "Remove the message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire le message privé."),
                                        new SubcommandData("tags", "List of tags you can use on your message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste des tags que vous pouvez utiliser sur votre message."),
                                        new SubcommandData("test", "Test your message !")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Test le message privé.")
                                )
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MESSAGE_SEND);

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

        String group = event.getSubcommandGroup();
        String method = event.getSubcommandName();

        if (method.equals("set")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + group + "messages WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    if (group.equals("dm")) {
                        try (PreparedStatement statement1 = connection.prepareStatement("UPDATE " + group + "messages SET message = ? WHERE guildID = ?")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, guild.getId());

                            statement1.executeUpdate();

                            event.reply(Language.getString("MESSAGE_SET_REPLACED", guild).replace("%GROUP%", group)).queue();
                        }
                    } else {
                        try (PreparedStatement statement1 = connection.prepareStatement("UPDATE " + group + "messages SET message = ?, channelID = ? WHERE guildID = ?")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getOption("channel", OptionMapping::getAsChannel).getId());
                            statement1.setString(3, guild.getId());

                            statement1.executeUpdate();

                            event.reply(Language.getString("MESSAGE_SET_REPLACED", guild).replace("%GROUP%", group)).queue();
                        }
                    }
                } else {
                    if (group.equals("dm")) {
                        try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO " + group + "messages (message, guildID) VALUES (?,?)")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, guild.getId());

                            statement1.executeUpdate();

                            connection.close();

                            event.reply(Language.getString("MESSAGE_SET_ADDED", guild).replace("%GROUP%", group)).queue();
                            return;
                        }
                    } else {
                        try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO " + group + "messages (message, channelID, guildID) VALUES (?,?,?)")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getOption("channel", OptionMapping::getAsChannel).getId());
                            statement1.setString(3, guild.getId());

                            statement1.executeUpdate();

                            event.reply(Language.getString("MESSAGE_SET_ADDED", guild).replace("%GROUP%", group)).queue();
                        }
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (method.equals("remove")) {

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + group + "messages WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("MESSAGE_REMOVE_NOMESSAGE", guild).replaceAll("%GROUP%", group)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                try (PreparedStatement statement1 = connection.prepareStatement("DELETE FROM " + group + "messages WHERE guildID = ?")) {
                    statement1.setString(1, guild.getId());

                    statement1.executeUpdate();

                    event.reply(Language.getString("MESSAGE_REMOVE_REMOVED", guild).replaceAll("%GROUP%", group)).queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (method.equals("tags")) {
            if (group.equals("join") || group.equals("dm"))
                event.reply(Language.getString("MESSAGE_TAGS1", guild)).setEphemeral(true).queue();
            else
                event.reply(Language.getString("MESSAGE_TAGS2", guild)).setEphemeral(true).queue();
        }

        if (method.equals("test")) {
            if (group.equals("join")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM joinmessages WHERE guildID = ?")) {
                    statement.setString(1, guild.getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        String channelID = result.getString(2);
                        TextChannel channel = guild.getTextChannelById(channelID);

                        assert channel != null;
                        channel.sendMessage(message
                                .replace("%USER%", event.getMember().getAsMention())
                                .replace("%USERNAME%", event.getUser().getEffectiveName())
                                .replace("%SERVERNAME%", guild.getName())
                                .replace("%MEMBERCOUNT%", String.valueOf(guild.getMemberCount()))).queue();
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (group.equals("dm")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM dmmessages WHERE guildID = ?")) {
                    statement.setString(1, guild.getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        event.getUser().openPrivateChannel().queue(dmChannel -> {
                            dmChannel.sendMessage(message
                                    .replace("%USER%", event.getMember().getAsMention())
                                    .replace("%USERNAME%", event.getUser().getEffectiveName())
                                    .replace("%SERVERNAME%", guild.getName())
                                    .replace("%MEMBERCOUNT%", String.valueOf(guild.getMemberCount()))
                            ).addActionRow(Button.secondary("sentfromguild", "Sent from " + guild.getName()).asDisabled()).queue();
                        });
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (group.equals("leave")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM leavemessages WHERE guildID = ?")) {
                    statement.setString(1, guild.getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        String channelID = result.getString(2);
                        TextChannel channel = guild.getTextChannelById(channelID);

                        assert channel != null;
                        channel.sendMessage(message
                                .replace("%USERNAME%", event.getUser().getEffectiveName())
                                .replace("%SERVERNAME%", guild.getName())
                                .replace("%MEMBERCOUNT%", String.valueOf(guild.getMemberCount()))).queue();
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            event.reply(Language.getString("MESSAGE_TEST_SUCCESS", guild)).setEphemeral(true).queue();
        }
    }
}
