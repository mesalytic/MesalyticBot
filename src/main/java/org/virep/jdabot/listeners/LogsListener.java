package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

import static org.virep.jdabot.utils.DatabaseUtils.getLogChannelID;
import static org.virep.jdabot.utils.DatabaseUtils.isEnabled;

public class LogsListener extends ListenerAdapter {
    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ChannelType type = event.getChannelType();

        if (type == ChannelType.PRIVATE | type == ChannelType.UNKNOWN) return;

        if (isEnabled("channelCreate", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(type.toString()).type + " channel created: " + event.getChannel().getAsMention() + "**")
                    .setColor(3066993)
                    .setFooter("Channel ID: " + event.getChannel().getId())
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setTimestamp(Instant.now())
                    .build();



            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelType type = event.getChannelType();

        if (type == ChannelType.PRIVATE | type == ChannelType.UNKNOWN) return;

        if (isEnabled("channelDelete", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(type.toString()).type + " channel delete: " + event.getChannel().getName() + "**")
                    .setColor(3066993)
                    .setFooter("Channel ID: " + event.getChannel().getId())
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setTimestamp(Instant.now())
                    .build();



            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    enum Type {
        CATEGORY("Category"),
        GUILD_NEWS_THREAD("News Thread"),
        GUILD_PRIVATE_THREAD("Private Thread"),
        GUILD_PUBLIC_THREAD("Public Thread"),
        NEWS("News"),
        STAGE("Stage"),
        TEXT("Text"),
        VOICE("Voice");

        public final String type;
        Type(String type) {
            this.type = type;
        }
    }
}
