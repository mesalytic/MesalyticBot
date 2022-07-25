package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCommand extends SlashCommand {
    public MessageCommand() {
        super("message", "Lets you configure join and leave messages.", false, new SubcommandGroupData[]{
                new SubcommandGroupData("join", "The bot will send a message whenever someone joins the server.").addSubcommands(
                        new SubcommandData("set", "Configure the join message.").addOptions(
                                new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true),
                                new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message join tags.", true)
                        ),
                        new SubcommandData("remove", "Remove the join message."),
                        new SubcommandData("tags", "List of tags you can use on your message.")
                ),
                new SubcommandGroupData("leave", "The bot will send a message whenever someone leaves the server.").addSubcommands(
                        new SubcommandData("set", "Configure the leave message.").addOptions(
                                new OptionData(OptionType.CHANNEL, "channel", "The channel where the message will be sent.", true),
                                new OptionData(OptionType.STRING, "message", "The message that will be sent. You can use tags, the list is at /message leave tags.", true )
                        ),
                        new SubcommandData("remove", "Remove the leave message."),
                        new SubcommandData("tags", "List of tags you can use on your message.")
                )
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String group = event.getSubcommandGroup();
        String method = event.getSubcommandName();

        if (method.equals("set")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM " + group + "messages WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("UPDATE " + group + "messages SET message = ?, channelID = ? WHERE guildID = ?")) {
                        updateStatement.setString(1, event.getOption("message").getAsString());
                        updateStatement.setString(2, event.getOption("channel").getAsChannel().getId());
                        updateStatement.setString(3, event.getGuild().getId());

                        updateStatement.executeUpdate();
                        event.reply("The " + group + " message has been successfully replaced.").queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("INSERT INTO " + group + "messages(message, channelID, guildID) VALUES (?,?,?)")) {
                        updateStatement.setString(1, event.getOption("message").getAsString());
                        updateStatement.setString(2, event.getOption("channel").getAsChannel().getId());
                        updateStatement.setString(3, event.getGuild().getId());

                        updateStatement.executeUpdate();
                        event.reply("The " + group + " message has been successfully added.").queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (method.equals("remove")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM " + group + "messages WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You don't have a " + group + " message set up. Set it up using `/message " + group + " set`").setEphemeral(true).queue();
                    return;
                }

                try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement("DELETE FROM " + group + "messages WHERE guildID = ?")) {
                    updateStatement.setString(1, event.getGuild().getId());

                    updateStatement.executeUpdate();
                    event.reply("The " + group + " message has been successfully removed.").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (method.equals("tags")) {
            if (group.equals("join")) event.reply("You can use tags to customize your message.\n\n%USER% - Pings the user\n%USERNAME% - Displays the username + discriminator of the user\n%SERVERNAME% - Displays the server name\n%MEMBERCOUNT% - Displays the member count\n\nYou can use Markdown to customize it even further.\nRole mentions and emoji usage are also supported (with custom emojis from your server)").setEphemeral(true).queue();
            else event.reply("You can use tags to customize your message.\n\n%USERNAME% - Displays the username + discriminator of the user\n%SERVERNAME% - Displays the server name\n%MEMBERCOUNT% - Displays the member count\n\nYou can use Markdown to customize it even further.\nRole mentions and emoji usage are also supported (with custom emojis from your server)").setEphemeral(true).queue();
        }
    }
}
