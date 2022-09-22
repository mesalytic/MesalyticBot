package org.virep.jdabot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.virep.jdabot.commands.general.RemindCommand;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.utils.Config;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.virep.jdabot.utils.Utils.timeStringToSeconds;

public class EventListener extends ListenerAdapter {

    private final WebhookClient webhook = WebhookClient.withUrl(Config.get("DISCORD_CMD_WEBHOOKURL"));

    @Override
    public void onShutdown(ShutdownEvent event) {
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

        System.out.println("Shutdown !");
        System.exit(0);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
            statement.setString(1, event.getAuthor().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM afk WHERE userID = ?")) {
                    deleteStatement.setString(1, event.getAuthor().getId());

                    deleteStatement.executeUpdate();

                    event.getMessage().reply(event.getAuthor().getAsMention() + ", your AFK status has been removed.").queue();
                }
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (event.getMessage().getMentions().getUsers().isEmpty()) return;
        User mentionedUser = event.getMessage().getMentions().getUsers().get(0);

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM afk WHERE userID = ?")) {
            statement.setString(1, mentionedUser.getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                event.getMessage().reply("**" + mentionedUser.getAsTag() + "** is AFK right now. Reason: " + result.getString("message")).setAllowedMentions(Collections.emptyList()).queue();
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM autorole WHERE guildID = ?");
             PreparedStatement statement2 = connection.prepareStatement("SELECT * FROM joinmessages WHERE guildID = ?")) {
            statement.setString(1, guild.getId());
            statement2.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();
            ResultSet resultJoin = statement2.executeQuery();

            if (result.next()) {
                String roleID = result.getString(1);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                guild.addRoleToMember(member, role).queue();
            }

            if (resultJoin.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

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

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM leavemessages WHERE guildID = ?")) {
            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

                assert channel != null;
                channel.sendMessage(message
                        .replace("%USERNAME%", event.getUser().getAsTag())
                        .replace("%SERVERNAME%", event.getGuild().getName())
                        .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        EmojiUnion fromFormattedEmoji = event.getEmoji();
        Emoji.Type emojiType = fromFormattedEmoji.getType();

        String emoji;
        if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
        else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            statement.setString(1, event.getMessageId());
            statement.setString(2, emoji);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String roleID = result.getString(2);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                assert member != null;

                event.getGuild().addRoleToMember(member, role).queue();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        Guild guild = event.getGuild();
        User member = event.getUser();

        EmojiUnion fromFormattedEmoji = event.getEmoji();
        Emoji.Type emojiType = fromFormattedEmoji.getType();

        String emoji;
        if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
        else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            statement.setString(1, event.getMessageId());
            statement.setString(2, emoji);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String roleID = result.getString(2);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                assert member != null;

                event.getGuild().removeRoleFromMember(member, role).queue();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonID = event.getButton().getId();

        if (buttonID.startsWith("interactionrole")) {
            String[] args = buttonID.split(":");

            String roleID = args[2];

            Role role = event.getGuild().getRoleById(roleID);

            if (event.getMember().getRoles().contains(role)) {
                event.getGuild().removeRoleFromMember(event.getUser(), role).queue();
                event.reply("The role has successfully been removed !").setEphemeral(true).queue();
            } else {
                event.getGuild().addRoleToMember(event.getUser(), role).queue();
                event.reply("You successfully got the role !").setEphemeral(true).queue();
            }


        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {

        String selectMenuID = event.getSelectMenu().getId();
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        List<SelectOption> allOptions = event.getSelectMenu().getOptions();

        if (selectMenuID.startsWith("selectmenurole")) {
            allOptions.forEach(option -> {
                String[] args = option.getValue().split(":");
                String roleID = args[2];

                Role role = event.getGuild().getRoleById(roleID);

                if (selectedOptions.contains(option)) {
                    event.getGuild().addRoleToMember(event.getUser(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getUser(), role).queue();
                }
            });

            event.reply("Your roles have successfully been updated.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
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

                if ((timestamp - Instant.now().getEpochSecond()) * 1000L < 0) {
                    user.openPrivateChannel().queue(dm -> {
                        dm.sendMessage("\uD83D\uDD59 - " + remindName).queue();

                        try (Connection connection1 = Database.getConnection(); PreparedStatement removeStatement = connection1.prepareStatement("DELETE FROM remind WHERE userID = ? AND name = ?")) {
                            removeStatement.setString(1, userID);
                            removeStatement.setString(2, remindName);

                            removeStatement.executeUpdate();
                            connection1.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
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
                                    e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event) {
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFFA500)
                .setDescription("Bot has reconnected.")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);
    }

    @Override
    public void onDisconnect(@NotNull DisconnectEvent event) {
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setDescription("Bot has disconnected.")
                .setTimestamp(Instant.now())
                .build();

        webhook.send(embed);
    }
}
