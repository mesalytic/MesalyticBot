package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateTopicEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

import static org.virep.jdabot.utils.DatabaseUtils.getLogChannelID;
import static org.virep.jdabot.utils.DatabaseUtils.isEnabled;
import static org.virep.jdabot.utils.Utils.secondsToSeperatedTime;

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

    @Override
    public void onChannelUpdateNSFW(ChannelUpdateNSFWEvent event) {
        if (isEnabled("channelNSFWUpdate", event.getGuild().getId())) {
            boolean oldValue = event.getOldValue();
            boolean newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel NSFW status changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", (oldValue ? "__**Enabled**__" : "__**Disabled**__"), true)
                    .addField("New:", (newValue ? "__**Enabled**__" : "__**Disabled**__"), true)
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
    public void onChannelUpdateTopic(ChannelUpdateTopicEvent event) {
        if (isEnabled("channelTopicUpdate", event.getGuild().getId())) {
            String oldValue = event.getOldValue();
            String newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel topic changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", (oldValue != null ? oldValue : "No Topic Specified"), true)
                    .addField("New:", (newValue != null ? newValue : "No Topic Specified"), true)
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
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        if (isEnabled("channelNameUpdate", event.getGuild().getId())) {
            String oldName = event.getOldValue();
            String newName = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel name changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", "**" + oldName + "**", true)
                    .addField("New:", "**" + newName + "**", true)
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
    public void onChannelUpdateSlowmode(ChannelUpdateSlowmodeEvent event) {
        if (isEnabled("channelSlowmodeUpdate", event.getGuild().getId())) {
            int oldValue = event.getOldValue();
            int newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel slowmode changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", "**" + secondsToSeperatedTime(oldValue) + "**", true)
                    .addField("New:", "**" + secondsToSeperatedTime(newValue) + "**", true)
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
