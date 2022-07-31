package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.virep.jdabot.Main;
import org.virep.jdabot.lavaplayer.GuildAudioManager;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EventListener extends ListenerAdapter {

    @Override
    public void onShutdown(ShutdownEvent event) {
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

        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (content.equals("jda!shutdown")) {
            JDA jda = event.getJDA();

            if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
                jda.getGuilds().forEach(g -> {
                    g.getAudioManager().closeAudioConnection();

                    GuildAudioManager gam = new GuildAudioManager(g);
                    gam.getPlayer().getLink().destroy();
                });

                event.getMessage().reply("Shutdown en cours").queue();

                jda.shutdown();
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String roleID = result.getString(1);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                guild.addRoleToMember(member, role).queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM joinmessages WHERE guildID = ?")) {
            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
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

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM leavemessages WHERE guildID = ?")) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            EmojiUnion fromFormattedEmoji = event.getEmoji();
            Emoji.Type emojiType = fromFormattedEmoji.getType();

            String emoji;
            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
            else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            Guild guild = event.getGuild();
            User member = event.getUser();

            EmojiUnion fromFormattedEmoji = event.getEmoji();
            Emoji.Type emojiType = fromFormattedEmoji.getType();

            String emoji;
            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
            else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

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
            event.getGuild().addRoleToMember(event.getUser(), role).queue();

            event.reply("You successfully got the role !").setEphemeral(true).queue();
        }
    }
}
