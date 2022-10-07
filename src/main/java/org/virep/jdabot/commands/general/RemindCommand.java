package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static org.virep.jdabot.utils.Utils.timeStringToSeconds;

public class RemindCommand implements Command {
    public static Map<String, Timer> timers = new HashMap<>();

    @Override
    public String getName() {
        return "remind";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Lets you configure reminders.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Permets de configurer des rappels.")
                .addSubcommands(
                        new SubcommandData("set", "Set a reminder")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configurer un rappel.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "content", "Content to remind about", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'évènement à rappeler."),
                                        new OptionData(OptionType.STRING, "when", "When to remind about", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Au bout de combien de temps faut-il envoyer le rappel?")
                                ),
                        new SubcommandData("remove", "Remove a reminder")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire un rappel.")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "index", "The index of the reminder", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La position du rappel.")
                                ),
                        new SubcommandData("list", "Lists your reminders.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste vos rappels.")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (event.getSubcommandName().equals("set")) {
            String userID = event.getUser().getId();
            String content = event.getOption("content", OptionMapping::getAsString);
            String when = event.getOption("when", OptionMapping::getAsString);

            long timestamp = Instant.now().getEpochSecond() + timeStringToSeconds(when);

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO remind (userID, name, timestamp) VALUES (?,?,?)")) {

                statement.setString(1, userID);
                statement.setString(2, content);
                statement.setLong(3, timestamp);

                statement.executeUpdate();

                event.reply(Language.getString("REMINDER_SET_SUCCESS", guild).replace("%CONTENT", MarkdownSanitizer.sanitize(content)).replace("%EPOCHSECONDS%", String.valueOf(Instant.now().getEpochSecond() + timeStringToSeconds(when)))).queue();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        event.getUser().openPrivateChannel().queue(dm -> {
                            dm.sendMessage(Language.getString("REMINDER", guild).replace("%CONTENT%", content)).queue();

                            try (Connection connection1 = Database.getConnection(); PreparedStatement removeStatement = connection1.prepareStatement("DELETE FROM remind WHERE userID = ? AND name = ?")) {
                                removeStatement.setString(1, event.getUser().getId());
                                removeStatement.setString(2, content);

                                removeStatement.executeUpdate();
                                connection1.close();
                            } catch (SQLException e) {
                                ErrorManager.handle(e, event);
                            }
                        });
                    }
                };

                Timer timer = new Timer(Instant.now().getEpochSecond() + timeStringToSeconds(when) + "-" + event.getUser().getId());

                timer.schedule(task, timeStringToSeconds(when) * 1000L);

                timers.put(Instant.now().getEpochSecond() + timeStringToSeconds(when) + "-" + event.getUser().getId(), timer);

                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (event.getSubcommandName().equals("list")) {
            StringBuilder sb = new StringBuilder();

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM remind WHERE userID = ? ORDER BY timestamp ASC")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                int i = 0;

                while (result.next()) {
                    i++;
                    sb
                            .append("**")
                            .append(i)
                            .append(")** `")
                            .append(MarkdownSanitizer.sanitize(result.getString("name")))
                            .append("` <t:")
                            .append((result.getLong("timestamp")))
                            .append(":F> (<t:")
                            .append((result.getLong("timestamp")))
                            .append(":R>)\n");
                }

                if (sb.isEmpty()) {
                    event.reply(Language.getString("REMINDER_LIST_NOREMINDERS", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                event.reply(sb.toString()).setAllowedMentions(Collections.emptyList()).queue();

                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }

        if (event.getSubcommandName().equals("remove")) {
            int index = event.getOption("index", OptionMapping::getAsInt);
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM remind WHERE userID = ? ORDER BY timestamp ASC")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                result.last();
                int resultSize = result.getRow();
                result.beforeFirst();

                if (index > resultSize) {
                    event.reply(Language.getString("REMINDER_REMOVE_OUTOFINDEX", guild)).setEphemeral(true).queue();
                    connection.close();
                    return;
                }

                if (result.first()) {
                    result.absolute(index);

                    String userID = result.getString("userID");
                    long timestamp = result.getLong("timestamp");

                    timers.get(timestamp + "-" + userID).cancel();
                    timers.remove(timestamp + "-" + userID);

                    try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM remind WHERE userID = ? AND name = ?")) {
                        deleteStatement.setString(1, event.getUser().getId());
                        deleteStatement.setString(2, result.getString("name"));

                        deleteStatement.executeQuery();

                        event.reply(Language.getString("REMINDER_REMOVE_REMOVED", guild).replace("%REMINDER%", MarkdownSanitizer.sanitize(result.getString("name")))).setAllowedMentions(Collections.emptyList()).queue();
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
