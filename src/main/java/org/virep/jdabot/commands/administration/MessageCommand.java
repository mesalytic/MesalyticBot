package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCommand implements Command {
    @Override
    public String getName() {
        return "message";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Lets you configure join and leave messages.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommandGroups(
                        new SubcommandGroupData("join", "The bot will send a message whenever someone joins the server.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the join message.").addOptions(
                                                new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true),
                                                new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message join tags.", true)
                                        ),
                                        new SubcommandData("remove", "Remove the join message."),
                                        new SubcommandData("tags", "List of tags you can use on your message."),
                                        new SubcommandData("test", "Test your message !")
                                ),
                        new SubcommandGroupData("leave", "The bot will send a message whenever someone leaves the server.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the leave message.").addOptions(
                                                new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true),
                                                new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message leave tags.", true)
                                        ),
                                        new SubcommandData("remove", "Remove the leave message."),
                                        new SubcommandData("tags", "List of tags you can use on your message."),
                                        new SubcommandData("test", "Test your message !")
                                ),
                        new SubcommandGroupData("dm", "The bot will send a DM whenever someone joins the server.")
                                .addSubcommands(
                                        new SubcommandData("set", "Configure the message.")
                                                .addOption(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message dm tags.", true),
                                        new SubcommandData("remove", "Remove the message."),
                                        new SubcommandData("tags", "List of tags you can use on your message."),
                                        new SubcommandData("test", "Test your message !")
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
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String group = event.getSubcommandGroup();
        String method = event.getSubcommandName();

        if (method.equals("set")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + group + "messages WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    if (group.equals("dm")) {
                        try (PreparedStatement statement1 = connection.prepareStatement("UPDATE " + group + "messages SET message = ? WHERE guildID = ?")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getGuild().getId());

                            statement1.executeUpdate();

                            event.reply("The " + group + " message has been successfully replaced.").queue();
                        }
                    } else {
                        try (PreparedStatement statement1 = connection.prepareStatement("UPDATE " + group + "messages SET message = ?, channelID = ? WHERE guildID = ?")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getOption("channel", OptionMapping::getAsChannel).getId());
                            statement1.setString(3, event.getGuild().getId());

                            statement1.executeUpdate();

                            event.reply("The " + group + " message has been successfully replaced.").queue();
                        }
                    }
                } else {
                    if (group.equals("dm")) {
                        try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO " + group + "messages (message, guildID) VALUES (?,?)")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getGuild().getId());

                            statement1.executeUpdate();

                            connection.close();

                            event.reply("The " + group + " message has been successfully added.").queue();
                            return;
                        }
                    } else {
                        try (PreparedStatement statement1 = connection.prepareStatement("INSERT INTO " + group + "messages (message, channelID, guildID) VALUES (?,?,?)")) {
                            statement1.setString(1, event.getOption("message", OptionMapping::getAsString));
                            statement1.setString(2, event.getOption("channel", OptionMapping::getAsChannel).getId());
                            statement1.setString(3, event.getGuild().getId());

                            statement1.executeUpdate();

                            event.reply("The " + group + " message has been successfully added.").queue();
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
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You don't have a " + group + " message set up. Set it up using `/message " + group + " set`").setEphemeral(true).queue();
                    return;
                }

                try (PreparedStatement statement1 = connection.prepareStatement("DELETE FROM " + group + "messages WHERE guildID = ?")) {
                    statement1.setString(1, event.getGuild().getId());

                    statement1.executeUpdate();

                    event.reply("The " + group + " message has been successfully removed.").queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (method.equals("tags")) {
            if (group.equals("join") || group.equals("dm"))
                event.reply("You can use tags to customize your message.\n\n%USER% - Pings the user\n%USERNAME% - Displays the username + discriminator of the user\n%SERVERNAME% - Displays the server name\n%MEMBERCOUNT% - Displays the member count\n\nYou can use Markdown to customize it even further.\nRole mentions and emoji usage are also supported (with custom emojis from your server)").setEphemeral(true).queue();
            else
                event.reply("You can use tags to customize your message.\n\n%USERNAME% - Displays the username + discriminator of the user\n%SERVERNAME% - Displays the server name\n%MEMBERCOUNT% - Displays the member count\n\nYou can use Markdown to customize it even further.\nRole mentions and emoji usage are also supported (with custom emojis from your server)").setEphemeral(true).queue();
        }

        if (method.equals("test")) {
            if (group.equals("join")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM joinmessages WHERE guildID = ?")) {
                    statement.setString(1, event.getGuild().getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        String channelID = result.getString(2);
                        TextChannel channel = event.getGuild().getTextChannelById(channelID);

                        assert channel != null;
                        channel.sendMessage(message
                                .replace("%USER%", event.getMember().getAsMention())
                                .replace("%USERNAME%", event.getUser().getAsTag())
                                .replace("%SERVERNAME%", event.getGuild().getName())
                                .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (group.equals("dm")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM dmmessages WHERE guildID = ?")) {
                    statement.setString(1, event.getGuild().getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        event.getUser().openPrivateChannel().queue(dmChannel -> {
                            dmChannel.sendMessage(message
                                    .replace("%USER%", event.getMember().getAsMention())
                                    .replace("%USERNAME%", event.getUser().getAsTag())
                                    .replace("%SERVERNAME%", event.getGuild().getName())
                                    .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
                        });
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (group.equals("leave")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM leavemessages WHERE guildID = ?")) {
                    statement.setString(1, event.getGuild().getId());

                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String message = result.getString(1);

                        String channelID = result.getString(2);
                        TextChannel channel = event.getGuild().getTextChannelById(channelID);

                        assert channel != null;
                        channel.sendMessage(message
                                .replace("%USERNAME%", event.getUser().getAsTag())
                                .replace("%SERVERNAME%", event.getGuild().getName())
                                .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
                    }

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            event.reply("Tested successfully.").setEphemeral(true).queue();
        }
    }
}
