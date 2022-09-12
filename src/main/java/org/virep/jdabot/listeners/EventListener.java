package org.virep.jdabot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
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
import org.virep.jdabot.database.Database;
import org.virep.jdabot.lavaplayer.GuildAudioManager;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class EventListener extends ListenerAdapter {

    private final WebhookClient webhook = WebhookClient.withUrl("https://canary.discord.com/api/webhooks/681148219148730389/pUnnf6VbfsnF7y7V8xrYs1xdnEbnRm58T3isOmffAZwBmZgArJFP8v2s5sEdMLodN9cc");

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

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM autorole WHERE guildID = " + guild.getId());

            if (result.next()) {
                String roleID = result.getString(1);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                guild.addRoleToMember(member, role).queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM joinmessages WHERE guildID = " + guild.getId());

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

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM dmmessages WHERE guildID = " + guild.getId());

            if (result.next()) {
                String message = result.getString(1);

                event.getMember().getUser().openPrivateChannel().queue(dmChannel -> {
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

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        try {
            ResultSet result = Database.executeQuery("SELECT * FROM leavemessages WHERE guildID = " + guild.getId());

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
        try {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            EmojiUnion fromFormattedEmoji = event.getEmoji();
            Emoji.Type emojiType = fromFormattedEmoji.getType();

            String emoji;
            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
            else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

            ResultSet result = Database.executeQuery("SELECT * FROM reactionRole WHERE messageID = " + event.getMessageId() + " AND emojIID = " + emoji);

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
        try {
            Guild guild = event.getGuild();
            User member = event.getUser();

            EmojiUnion fromFormattedEmoji = event.getEmoji();
            Emoji.Type emojiType = fromFormattedEmoji.getType();

            String emoji;
            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
            else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

            ResultSet result = Database.executeQuery("SELECT * FROM reactionRole WHERE messageID = " + event.getMessageId() + " AND emojiID = " + emoji);

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
