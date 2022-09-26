package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;
import org.virep.jdabot.utils.Utils;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


public class WarnCommand implements Command {
    @Override
    public String getName() {
        return "warn";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Warn people")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                .addSubcommandGroups(
                        new SubcommandGroupData("config", "Configure warns")
                                .addSubcommands(
                                        new SubcommandData("timeout", "Configure the amount of warn needed for a timeout.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "timeout_amount", "Warn amount needed for the event to happen.", true).setMinValue(0),
                                                        new OptionData(OptionType.STRING, "duration", "Timeout duration", true)
                                                ),
                                        new SubcommandData("kick", "Configure the amount of warn needed for a kick.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "kick_amount", "Warn amount needed for the event to happen.", true).setMinValue(0)
                                                ),
                                        new SubcommandData("ban", "Configure the amount of warn needed for a ban.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "ban_amount", "Warn amount needed for the event to happen.", true).setMinValue(0)
                                                ),
                                        new SubcommandData("channel", "Configure the channel where warns will be logged")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "Channel where warns will be logged", true)
                                                )
                                )
                )
                .addSubcommands(
                        new SubcommandData("set", "Set a warn to someone")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to warn.", true),
                                        new OptionData(OptionType.STRING, "reason", "The reason for the warn.")
                                ),
                        new SubcommandData("list", "List all warns from a user")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to view the warns", true)
                                ),
                        new SubcommandData("remove", "Remove a specific warn from a user")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to remove the warn", true),
                                        new OptionData(OptionType.INTEGER, "index", "The index of the warn to remove", true)
                                )
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String group = event.getSubcommandGroup();
        String subcommand = event.getSubcommandName();

        if (group != null && group.equals("config")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM warn_config WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    switch (subcommand) {
                        case "timeout" -> {
                            int amount = event.getOption("timeout_amount", OptionMapping::getAsInt);
                            int timeout = Utils.timeStringToSeconds(event.getOption("duration", OptionMapping::getAsString));

                            if (timeout > Member.MAX_TIME_OUT_LENGTH * 86400) {
                                event.reply("The time duration you specified is above 28 days (2419200 seconds), please specify a lower time duration.").setEphemeral(true).queue();
                                connection.close();
                                return;
                            }

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET timeout = ?, timeout_duration = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setInt(2, timeout);
                                updateStatement.setString(3, event.getGuild().getId());

                                updateStatement.executeUpdate();

                                event.reply("Members will now be timed-out if they have **" + amount + "** warns for **" + event.getOption("duration", OptionMapping::getAsString) + "**").queue();
                            }
                            break;
                        }
                        case "kick" -> {
                            int amount = event.getOption("kick_amount", OptionMapping::getAsInt);

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET " + subcommand + " = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setString(2, event.getGuild().getId());

                                updateStatement.executeUpdate();

                                event.reply("Members will now be kicked if they have **" + amount + "** warns").queue();
                            }
                            break;
                        }
                        case "ban" -> {
                            int amount = event.getOption("ban_amount", OptionMapping::getAsInt);

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET " + subcommand + " = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setString(2, event.getGuild().getId());

                                updateStatement.executeUpdate();

                                event.reply("Members will now be banned if they have **" + amount + "** warns").queue();
                            }
                            break;
                        }
                        default -> {
                            GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET channelID = ? WHERE guildID = ?")) {
                                updateStatement.setString(1, channel.getId());
                                updateStatement.setString(2, event.getGuild().getId());

                                updateStatement.executeUpdate();

                                event.reply("Warns will now be logged in " + channel.getAsMention()).queue();
                            }
                        }
                    }
                } else {
                    String channelID = event.getOption("channel") != null ? event.getOption("channel", OptionMapping::getAsChannel).getId() : null;
                    int timeoutAmount = event.getOption("timeout_amount") != null ? event.getOption("timeout_amount", OptionMapping::getAsInt) : 0;
                    int timeoutDuration = event.getOption("timeout_duration") != null ? Utils.timeStringToSeconds(event.getOption("timeout_duration", OptionMapping::getAsString)) : 0;
                    int kickAmount = event.getOption("kick_amount") != null ? event.getOption("kick_amount", OptionMapping::getAsInt) : 0;
                    int banAmount = event.getOption("ban_amount") != null ? event.getOption("ban_amount", OptionMapping::getAsInt) : 0;

                    String insertString = "";

                    switch (subcommand) {
                        case "timeout" ->
                                insertString = "Members will now be timed-out if they have **" + timeoutAmount + "** warns for **" + event.getOption("timeout_duration", OptionMapping::getAsString) + "**";
                        case "kick" ->
                                insertString = "Members will now be kicked if they have **" + kickAmount + "** warns";
                        case "ban" ->
                                insertString = "Members will now be banned if they have **" + banAmount + "** warns";
                        case "channel" ->
                                insertString = "Warns will now be logged in " + event.getOption("channel", OptionMapping::getAsChannel).getAsMention();
                    }

                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO warn_config (guildID, channelID, timeout, timeout_duration, kick, ban) VALUES (?,?,?,?,?,?)")) {
                        insertStatement.setString(1, event.getGuild().getId());
                        insertStatement.setString(2, channelID);
                        insertStatement.setInt(3, timeoutAmount);
                        insertStatement.setInt(4, timeoutDuration);
                        insertStatement.setInt(5, kickAmount);
                        insertStatement.setInt(6, banAmount);

                        insertStatement.executeUpdate();

                        event.reply(insertString).queue();
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        } else if (subcommand.equals("set")) {
            Member member = event.getOption("user", OptionMapping::getAsMember);
            String reason = event.getOption("reason") != null ? event.getOption("reason", OptionMapping::getAsString) : "No reason specified";

            try (Connection connection = Database.getConnection();
                 PreparedStatement amountStatement = connection.prepareStatement("SELECT * FROM warn_amount WHERE guildID = ? AND userID = ?");
                 PreparedStatement configStatement = connection.prepareStatement("SELECT * FROM warn_config WHERE guildID = ?")) {

                amountStatement.setString(1, event.getGuild().getId());
                amountStatement.setString(2, member.getId());

                configStatement.setString(1, event.getGuild().getId());

                ResultSet amountResult = amountStatement.executeQuery();
                ResultSet configResult = configStatement.executeQuery();

                if (amountResult.first()) {
                    try (PreparedStatement amountUpdateStatement = connection.prepareStatement("UPDATE warn_amount SET amount = ? WHERE guildID = ? AND userID = ?")) {
                        amountUpdateStatement.setInt(1, (amountResult.getInt("amount") + 1));
                        amountUpdateStatement.setString(2, event.getGuild().getId());
                        amountUpdateStatement.setString(3, member.getId());


                        amountUpdateStatement.executeUpdate();

                        if (configResult.first()) {
                            int amount = amountResult.getInt("amount");

                            if (amount + 1 >= configResult.getInt("ban"))
                                member.ban(0, TimeUnit.SECONDS).reason("Automatic ban due to warns : " + reason);
                            else if (amount + 1 >= configResult.getInt("kick"))
                                member.kick().reason("Automatic kick due to warns : " + reason);
                            else if (amount + 1 >= configResult.getInt("timeout"))
                                member.timeoutFor(configResult.getInt("timeout_duration"), TimeUnit.SECONDS).reason("Automatic timeout due to warns : " + reason);

                            if (configResult.getString("channelID") != null) {
                                TextChannel channel = event.getGuild().getTextChannelById(configResult.getString("channelID"));

                                MessageEmbed embed = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(Color.RED)
                                        .setTitle("Warn #" + (amount + 1))
                                        .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                                        .addField("User warned: ", member.getUser().getAsTag(), true)
                                        .addField("Warned by : ", event.getUser().getAsTag(), true)
                                        .addField("Reason", reason, true)
                                        .build();

                                channel.sendMessageEmbeds(embed).queue();
                            }
                        }
                    }
                } else {
                    if (configResult.first() && configResult.getString("channelID") != null) {
                        TextChannel channel = event.getGuild().getTextChannelById(configResult.getString("channelID"));

                        MessageEmbed embed = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(Color.RED)
                                .setTitle("Warn #1")
                                .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                                .addField("User warned: ", member.getUser().getAsTag(), true)
                                .addField("Warned by : ", event.getUser().getAsTag(), true)
                                .addField("Reason", reason, true)
                                .build();

                        channel.sendMessageEmbeds(embed).queue();
                    }

                    try (PreparedStatement amountInsertStatement = connection.prepareStatement("INSERT INTO warn_amount (guildID, userID, amount) VALUES (?,?,?)")) {
                        amountInsertStatement.setString(1, event.getGuild().getId());
                        amountInsertStatement.setString(2, member.getId());
                        amountInsertStatement.setInt(3, 1);

                        amountInsertStatement.executeUpdate();
                    }
                }

                event.reply("**" + member.getUser().getAsTag() + "** has been warned for the following reason : **" + reason + "**").queue();

                try (PreparedStatement insertReasonStatement = connection.prepareStatement("INSERT INTO warn_reasons (guildID, userID, reason, timestamp) VALUES (?,?,?,?)")) {
                    insertReasonStatement.setString(1, event.getGuild().getId());
                    insertReasonStatement.setString(2, member.getId());
                    insertReasonStatement.setString(3, reason);
                    insertReasonStatement.setInt(4, (int) Instant.now().getEpochSecond());

                    insertReasonStatement.executeUpdate();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        } else if (subcommand.equals("list")) {
            Member member = event.getOption("user", OptionMapping::getAsMember);

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM warn_reasons WHERE guildID = ? AND userID = ? ORDER BY timestamp ASC")) {
                statement.setString(1, event.getGuild().getId());
                statement.setString(2, member.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("This user has no warns.").setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
                        .setTitle("Warn list for " + member.getUser().getAsTag())
                        .setTimestamp(Instant.now());

                StringBuilder embedDescription = new StringBuilder();

                result.beforeFirst();

                while (result.next()) {
                    embedDescription.append("**")
                            .append(result.getRow())
                            .append(")** ")
                            .append(result.getString("reason"))
                            .append(" - <t:")
                            .append(result.getInt("timestamp"))
                            .append(":R>\n");
                }

                embedBuilder.setDescription(embedDescription.toString());

                event.replyEmbeds(embedBuilder.build()).queue();

                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        } else if (subcommand.equals("remove")) {
            int index = event.getOption("index", OptionMapping::getAsInt);
            Member member = event.getOption("user", OptionMapping::getAsMember);

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM warn_reasons WHERE guildID = ? AND userID = ?");
                 PreparedStatement amountStatement = connection.prepareStatement("SELECT * FROM warn_amount WHERE guildID = ? AND userID = ?")) {
                statement.setString(1, event.getGuild().getId());
                statement.setString(2, member.getId());

                amountStatement.setString(1, event.getGuild().getId());
                amountStatement.setString(2, member.getId());

                ResultSet result = statement.executeQuery();
                ResultSet amountResult = amountStatement.executeQuery();

                if (!result.first()) {
                    event.reply("This member has no warns.").setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                result.beforeFirst();
                result.last();
                int resultSize = result.getRow();
                result.beforeFirst();

                if (index > resultSize) {
                    event.reply("The number you specified is over the number of warns this member has.").setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                result.absolute(index);
                amountResult.first();

                try (PreparedStatement deleteReasonStatement = connection.prepareStatement("DELETE FROM warn_reasons WHERE guildID = ? AND userID = ? AND reason = ?");
                     PreparedStatement updateAmountStatement = connection.prepareStatement("UPDATE warn_amount SET amount = ? WHERE guildID = ? AND userID = ?")) {
                    deleteReasonStatement.setString(1, event.getGuild().getId());
                    deleteReasonStatement.setString(2, member.getId());
                    deleteReasonStatement.setString(3, result.getString("reason"));

                    updateAmountStatement.setInt(1, amountResult.getInt("amount") - 1);
                    updateAmountStatement.setString(2, event.getGuild().getId());
                    updateAmountStatement.setString(3, member.getId());

                    deleteReasonStatement.executeUpdate();
                    updateAmountStatement.executeUpdate();

                    event.reply("The warn has been successfully removed.").queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
