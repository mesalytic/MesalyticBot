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
            try {
                ResultSet result = Database.executeQuery("SELECT * FROM " + group + "messages WHERE guildID = " + event.getGuild().getId());

                if (result.first()) {
                    if (group.equals("dm")) {
                        Database.executeQuery("UPDATE " + group + "messages SET message = " + event.getOption("message", OptionMapping::getAsString) + "WHERE guildID = " + event.getGuild().getId());
                        event.reply("The " + group + " message has been successfully replaced.").queue();
                        return;
                    }

                    Database.executeQuery("UPDATE " + group + "messages SET message = " + event.getOption("message", OptionMapping::getAsString) + ", channelID = " + event.getOption("channel", OptionMapping::getAsChannel).getId() + " WHERE guildID = " + event.getGuild().getId());
                    event.reply("The " + group + " message has been successfully replaced.").queue();
                } else {
                    if (group.equals("dm")) {

                        Database.executeQuery("INSERT INTO " + group + "messages (message, guildID) VALUES (" + event.getOption("message").getAsString() + "," + event.getGuild().getId() + ")");

                        event.reply("The " + group + " message has been successfully added.").queue();
                        return;
                    }

                    Database.executeQuery("INSERT INTO " + group + "messages (message, channelID, guildID) VALUES (" + event.getOption("message").getAsString() + "," + event.getOption("channel").getAsChannel().getId() + "," + event.getGuild().getId() + ")");

                    event.reply("The " + group + " message has been successfully added.").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (method.equals("remove")) {
            try {
                ResultSet result = Database.executeQuery("SELECT * FROM " + group + "messages WHERE guildID = ?");

                if (!result.first()) {
                    event.reply("You don't have a " + group + " message set up. Set it up using `/message " + group + " set`").setEphemeral(true).queue();
                    return;
                }

                Database.executeQuery("DELETE FROM " + group + "messages WHERE guildID = " + event.getGuild().getId());

                event.reply("The " + group + " message has been successfully removed.").queue();
            } catch (SQLException e) {
                e.printStackTrace();
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
                try {
                    ResultSet result = Database.executeQuery("SELECT * FROM joinmessages WHERE guildID = " + event.getGuild().getId());

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (group.equals("dm")) {
                try {
                    ResultSet result = Database.executeQuery("SELECT * FROM dmmessages WHERE guildID = " + event.getGuild().getId());

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (group.equals("leave")) {
                try {
                    ResultSet result = Database.executeQuery("SELECT * FROM leavemessages WHERE guildID = " + event.getGuild().getId());

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            event.reply("Tested successfully.").setEphemeral(true).queue();
        }
    }
}
