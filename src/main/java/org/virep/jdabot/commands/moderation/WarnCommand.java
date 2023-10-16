package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.Command;
import org.virep.jdabot.utils.ErrorManager;
import org.virep.jdabot.utils.Utils;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class WarnCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(WarnCommand.class);

    @Override
    public String getName() {
        return "warn";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Warn people")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                .addSubcommandGroups(
                        new SubcommandGroupData("config", "Configure warns")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le système d'avertissements.")
                                .addSubcommands(
                                        new SubcommandData("timeout", "Configure the amount of warn needed for a timeout.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le nombre d'avertissement nécéssaires pour une mise en sourdine.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "timeout_amount", "Warn amount needed for a timeout.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Nombre d'avertissement nécéssaire pour une mise en sourdine.")
                                                                .setMinValue(0),
                                                        new OptionData(OptionType.STRING, "duration", "Timeout duration (max 28d)", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Durée de la mise en sourdine (max 28 jours)")
                                                ),
                                        new SubcommandData("kick", "Configure the amount of warn needed for a kick.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le nombre d'avertissement nécéssaire pour une exclusion.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "kick_amount", "Warn amount needed for a kick.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Nombre d'avertissement nécéssaire pour une exclusion.")
                                                                .setMinValue(0)
                                                ),
                                        new SubcommandData("ban", "Configure the amount of warn needed for a ban.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le nombre d'avertissement nécéssaire pour un bannissement.")
                                                .addOptions(
                                                        new OptionData(OptionType.INTEGER, "ban_amount", "Warn amount needed for a ban.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Nombre d'avertissement nécéssaire pour un bannissement.")
                                                                .setMinValue(0)
                                                ),
                                        new SubcommandData("channel", "Configure the channel where warns will be logged")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure le salon auquel les avertissements seront envoyés.")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "Channel where warns will be logged", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon auquel les avertissements seront envoyés.")
                                                )
                                )
                )
                .addSubcommands(
                        new SubcommandData("set", "Set a warn to someone")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Donne un avertissement a quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to warn.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre a avertir."),
                                        new OptionData(OptionType.STRING, "reason", "The reason for the warn.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison de l'avertissement.")
                                ),
                        new SubcommandData("list", "List all warns from a user")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste tous les avertissement d'un membre.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to view the warns", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre à voir les avertissements.")
                                ),
                        new SubcommandData("remove", "Remove a specific warn from a user")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire un avertissement à un membre.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user to remove the warn", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre auquel l'avertissement sera retiré."),
                                        new OptionData(OptionType.INTEGER, "index", "The index of the warn to remove", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'index de l'avertissement à retirer.")
                                )
                );
    }

    @Override
    public java.util.List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MODERATE_MEMBERS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS, Permission.VIEW_AUDIT_LOGS, Permission.VIEW_CHANNEL);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (!event.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        String group = event.getSubcommandGroup();
        String subcommand = event.getSubcommandName();

        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.getChannel().sendMessage(Language.getString("ERRORHANDLER_MISSINGPERMISSIONS", guild)).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_USER),
                        (ex) -> event.getChannel().sendMessage(Language.getString("ERRORHANDLER_UNKNOWNUSER", guild)).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_MEMBER),
                        (ex) -> event.getChannel().sendMessage(Language.getString("ERRORHANDLER_UNKNOWNUSER", guild)).queue());

        if (group != null && group.equals("config")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM warn_config WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    switch (subcommand) {
                        case "timeout" -> {
                            int amount = event.getOption("timeout_amount", OptionMapping::getAsInt);
                            int timeout = Utils.timeStringToSeconds(event.getOption("duration", OptionMapping::getAsString));

                            if (timeout > Member.MAX_TIME_OUT_LENGTH * 86400) {
                                event.reply(Language.getString("WARN_MAXTIMEOUT", guild)).setEphemeral(true).queue();
                                connection.close();
                                return;
                            }

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET timeout = ?, timeout_duration = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setInt(2, timeout);
                                updateStatement.setString(3, guild.getId());

                                updateStatement.executeUpdate();

                                event.reply(Language.getString("WARN_CONFIG_TIMEOUT", guild).replace("%AMOUNT%", String.valueOf(amount)).replace("%DURATION%", event.getOption("duration", OptionMapping::getAsString))).queue();
                            }
                            break;
                        }
                        case "kick" -> {
                            int amount = event.getOption("kick_amount", OptionMapping::getAsInt);

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET " + subcommand + " = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setString(2, guild.getId());

                                updateStatement.executeUpdate();

                                event.reply(Language.getString("WARN_CONFIG_KICK", guild).replace("%AMOUNT%", String.valueOf(amount))).queue();
                            }
                            break;
                        }
                        case "ban" -> {
                            int amount = event.getOption("ban_amount", OptionMapping::getAsInt);

                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET " + subcommand + " = ? WHERE guildID = ?")) {
                                updateStatement.setInt(1, amount);
                                updateStatement.setString(2, guild.getId());

                                updateStatement.executeUpdate();

                                event.reply(Language.getString("WARN_CONFIG_BAN", guild).replace("%AMOUNT%", String.valueOf(amount))).queue();
                            }
                            break;
                        }
                        default -> {
                            GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
                            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE warn_config SET channelID = ? WHERE guildID = ?")) {
                                updateStatement.setString(1, channel.getId());
                                updateStatement.setString(2, guild.getId());

                                updateStatement.executeUpdate();

                                event.reply(Language.getString("WARN_CONFIG_CHANNEL", guild).replace("%CHANNELMENTION%", channel.getAsMention())).queue();
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
                                insertString = Language.getString("WARN_CONFIG_TIMEOUT", guild).replace("%AMOUNT%", String.valueOf(timeoutAmount)).replace("%DURATION%", String.valueOf(timeoutDuration));
                        case "kick" ->
                                insertString = Language.getString("WARN_CONFIG_KICK", guild).replace("%AMOUNT%", String.valueOf(kickAmount));
                        case "ban" ->
                                insertString = Language.getString("WARN_CONFIG_BAN", guild).replace("%AMOUNT%", String.valueOf(banAmount));
                        case "channel" ->
                                insertString = Language.getString("WARN_CONFIG_CHANNEL", guild).replace("%CHANNELMENTION%", event.getOption("channel", OptionMapping::getAsChannel).getAsMention());
                    }

                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO warn_config (guildID, channelID, timeout, timeout_duration, kick, ban) VALUES (?,?,?,?,?,?)")) {
                        insertStatement.setString(1, guild.getId());
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

            if (member.isOwner()) {
                event.reply(Language.getString("WARN_OWNER", guild)).setEphemeral(true).queue();
                return;
            }

            if (member.getId().equals(event.getMember().getId())) {
                event.reply(Language.getString("WARN_YOURSELF", guild)).setEphemeral(true).queue();
                return;
            }

            if (member.getUser().isBot()) {
                event.reply(Language.getString("WARN_BOT", guild)).setEphemeral(true).queue();
                return;
            }

            if (member.hasPermission(Permission.MODERATE_MEMBERS)) {
                event.reply(Language.getString("WARN_MODERATOR", guild)).setEphemeral(true).queue();
                return;
            }

            try (Connection connection = Database.getConnection();
                 PreparedStatement amountStatement = connection.prepareStatement("SELECT * FROM warn_amount WHERE guildID = ? AND userID = ?");
                 PreparedStatement configStatement = connection.prepareStatement("SELECT * FROM warn_config WHERE guildID = ?")) {

                amountStatement.setString(1, guild.getId());
                amountStatement.setString(2, member.getId());

                configStatement.setString(1, guild.getId());

                ResultSet amountResult = amountStatement.executeQuery();
                ResultSet configResult = configStatement.executeQuery();

                if (amountResult.first()) {
                    try (PreparedStatement amountUpdateStatement = connection.prepareStatement("UPDATE warn_amount SET amount = ? WHERE guildID = ? AND userID = ?")) {
                        amountUpdateStatement.setInt(1, (amountResult.getInt("amount") + 1));
                        amountUpdateStatement.setString(2, guild.getId());
                        amountUpdateStatement.setString(3, member.getId());


                        amountUpdateStatement.executeUpdate();

                        if (configResult.first()) {
                            int amount = amountResult.getInt("amount");

                            if (amount + 1 >= configResult.getInt("ban"))
                                member.ban(0, TimeUnit.SECONDS).reason(Language.getString("WARN_AUTOBAN", guild).replace("%REASON%", reason)).queue(success -> log.info("banned due to warn"), errorHandler);
                            else if (amount + 1 >= configResult.getInt("kick"))
                                member.kick().reason(Language.getString("WARN_AUTOKICK", guild).replace("%REASON%", reason)).queue(success -> log.info("kicked due to warn"), errorHandler);
                            else if (amount + 1 >= configResult.getInt("timeout"))
                                member.timeoutFor(configResult.getInt("timeout_duration"), TimeUnit.SECONDS).reason(Language.getString("WARN_AUTOTIMEOUT", guild).replace("%REASON%", reason)).queue(success -> log.info("timed out due to warn"), errorHandler);

                            if (configResult.getString("channelID") != null) {
                                TextChannel channel = guild.getTextChannelById(configResult.getString("channelID"));

                                MessageEmbed embed = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(Color.RED)
                                        .setTitle(Language.getString("WARN_EMBEDTITLE", guild).replace("%AMOUNT%", String.valueOf((amount + 1))))
                                        .setAuthor(member.getUser().getEffectiveName(), null, member.getUser().getAvatarUrl())
                                        .addField(Language.getString("WARN_EMBEDWARNED", guild), member.getUser().getEffectiveName(), true)
                                        .addField(Language.getString("WARN_EMBEDWARNEDBY", guild), event.getUser().getEffectiveName(), true)
                                        .addField(Language.getString("WARN_EMBEDREASON", guild), reason, true)
                                        .build();

                                channel.sendMessageEmbeds(embed).queue();
                            }
                        }
                    }
                } else {
                    if (configResult.first() && configResult.getString("channelID") != null) {
                        TextChannel channel = guild.getTextChannelById(configResult.getString("channelID"));

                        MessageEmbed embed = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(Color.RED)
                                .setTitle(Language.getString("WARN_EMBEDTITLE", guild).replace("%AMOUNT%", String.valueOf(1)))
                                .setAuthor(member.getUser().getEffectiveName(), null, member.getUser().getAvatarUrl())
                                .addField(Language.getString("WARN_EMBEDWARNED", guild), member.getUser().getEffectiveName(), true)
                                .addField(Language.getString("WARN_EMBEDWARNEDBY", guild), event.getUser().getEffectiveName(), true)
                                .addField(Language.getString("WARN_EMBEDREASON", guild), reason, true)
                                .build();

                        channel.sendMessageEmbeds(embed).queue();
                    }

                    try (PreparedStatement amountInsertStatement = connection.prepareStatement("INSERT INTO warn_amount (guildID, userID, amount) VALUES (?,?,?)")) {
                        amountInsertStatement.setString(1, guild.getId());
                        amountInsertStatement.setString(2, member.getId());
                        amountInsertStatement.setInt(3, 1);

                        amountInsertStatement.executeUpdate();
                    }
                }

                event.reply(Language.getString("WARN_WARNED", guild).replace("%USERTAG%", member.getUser().getEffectiveName()).replace("%REASON%", reason)).queue();

                try (PreparedStatement insertReasonStatement = connection.prepareStatement("INSERT INTO warn_reasons (guildID, userID, reason, timestamp) VALUES (?,?,?,?)")) {
                    insertReasonStatement.setString(1, guild.getId());
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
                statement.setString(1, guild.getId());
                statement.setString(2, member.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("WARN_LIST_NOWARNS", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getAvatarUrl())
                        .setTitle(Language.getString("WARN_LIST_EMBEDTITLE", guild).replace("%USERTAG%", member.getUser().getEffectiveName()))
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
                statement.setString(1, guild.getId());
                statement.setString(2, member.getId());

                amountStatement.setString(1, guild.getId());
                amountStatement.setString(2, member.getId());

                ResultSet result = statement.executeQuery();
                ResultSet amountResult = amountStatement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("WARN_LIST_NOWARNS", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                result.beforeFirst();
                result.last();
                int resultSize = result.getRow();
                result.beforeFirst();

                if (index > resultSize) {
                    event.reply(Language.getString("WARN_LIST_OUTINDEX", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                result.absolute(index);
                amountResult.first();

                try (PreparedStatement deleteReasonStatement = connection.prepareStatement("DELETE FROM warn_reasons WHERE guildID = ? AND userID = ? AND reason = ?");
                     PreparedStatement updateAmountStatement = connection.prepareStatement("UPDATE warn_amount SET amount = ? WHERE guildID = ? AND userID = ?")) {
                    deleteReasonStatement.setString(1, guild.getId());
                    deleteReasonStatement.setString(2, member.getId());
                    deleteReasonStatement.setString(3, result.getString("reason"));

                    updateAmountStatement.setInt(1, amountResult.getInt("amount") - 1);
                    updateAmountStatement.setString(2, guild.getId());
                    updateAmountStatement.setString(3, member.getId());

                    deleteReasonStatement.executeUpdate();
                    updateAmountStatement.executeUpdate();

                    event.reply(Language.getString("WARN_REMOVE_REMOVED", guild)).queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
