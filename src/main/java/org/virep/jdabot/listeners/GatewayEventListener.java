package org.virep.jdabot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.commands.general.RemindCommand;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class GatewayEventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(GatewayEventListener.class);
    private final WebhookClient webhook = WebhookClient.withUrl(Config.get("DISCORD_STATUS_WEBHOOKURL"));

    @Override
    public void onShutdown(ShutdownEvent event) {
        log.info("Shutdown instantiated...");
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xff0000)
                .setDescription("Shutdown Instantiated.")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);

        JDA jda = event.getJDA();

        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g -> {
                AudioManager manager = g.getAudioManager();
                manager.getConnectionStatus();
                if (manager.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                    g.getAudioManager().closeAudioConnection();

                    GuildAudioManager gam = new GuildAudioManager(g);
                    gam.getPlayer().getLink().destroy();

                }
            });
            jda.shutdown();
        }

        log.info("Successfully shutdown.");
        System.exit(0);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Bot has successfully started !");

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0x00ff00)
                .setDescription("Bot has started")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);

        JDA jda = event.getJDA();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM remind")) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String remindName = result.getString("name");
                String userID = result.getString("userID");
                long timestamp = result.getLong("timestamp");

                User user = jda.getUserById(userID);

                if (user == null) {
                    log.warn("Reminder data from unknown user " + userID + " has been found");
                    continue;
                }

                if ((timestamp - Instant.now().getEpochSecond()) * 1000L < 0) {
                    user.openPrivateChannel().queue(dm -> {
                        dm.sendMessage("\uD83D\uDD59 - Reminder for: **" + remindName + "**").queue();

                        try (Connection connection1 = Database.getConnection(); PreparedStatement removeStatement = connection1.prepareStatement("DELETE FROM remind WHERE userID = ? AND name = ?")) {
                            removeStatement.setString(1, userID);
                            removeStatement.setString(2, remindName);

                            removeStatement.executeUpdate();
                            connection1.close();
                        } catch (SQLException e) {
                            ErrorManager.handleNoEvent(e);
                        }
                    });
                } else {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            user.openPrivateChannel().queue(dm -> {
                                dm.sendMessage("\uD83D\uDD59 - " + remindName).queue();

                                try (Connection connection1 = Database.getConnection(); PreparedStatement removeStatement = connection1.prepareStatement("DELETE FROM remind WHERE userID = ? AND name = ?")) {
                                    removeStatement.setString(1, userID);
                                    removeStatement.setString(2, remindName);

                                    removeStatement.executeUpdate();
                                    connection1.close();
                                } catch (SQLException e) {
                                    ErrorManager.handleNoEvent(e);
                                }
                            });
                        }
                    };

                    Timer timer = new Timer(timestamp + "-" + userID);

                    timer.schedule(task, (timestamp - Instant.now().getEpochSecond()) * 1000L);

                    RemindCommand.timers.put(timestamp + "-" + userID, timer);
                }
                connection.close();
            }
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        log.info("Events have been successfully resumed");
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFFA500)
                .setDescription("Events have been successfully resumed")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        log.info("Bot has successfully reconnected.");
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFFA500)
                .setDescription("Bot has reconnected.")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);
    }

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {
        log.warn("Bot has disconnected.");

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setDescription("Bot has disconnected.")
                .addField(new WebhookEmbed.EmbedField(true, "Close Reason", event.getClientCloseFrame().getCloseReason()))
                .addField(new WebhookEmbed.EmbedField(true, "Close Code", event.isClosedByServer() ? String.valueOf(event.getCloseCode().getCode()) : "N/A"))
                .addField(new WebhookEmbed.EmbedField(true, "Close Code Meaning", event.isClosedByServer() ? event.getCloseCode().getMeaning() : "N/A"))
                .addField(new WebhookEmbed.EmbedField(true, "Closed by Server?", String.valueOf(event.isClosedByServer())))
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);
    }
}
