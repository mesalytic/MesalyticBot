package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateTopicEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static org.virep.jdabot.utils.DatabaseUtils.getLogChannelID;
import static org.virep.jdabot.utils.DatabaseUtils.isEnabled;
import static org.virep.jdabot.utils.Utils.secondsToSeperatedTime;

public class LogsListener extends ListenerAdapter {

    // Map<messageID, messageContent>
    private final Map<Long, Message> messageMap = new HashMap<>();

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ChannelType type = event.getChannelType();

        if (type == ChannelType.PRIVATE | type == ChannelType.UNKNOWN) return;

        if (isEnabled("channelCreate", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**" + Type.valueOf(type.toString()).type + " channel created: " + event.getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
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
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(15158332)
                    .setDescription("**" + Type.valueOf(type.toString()).type + " channel delete: " + event.getChannel().getName() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
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
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel NSFW status changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", (oldValue ? "__**Enabled**__" : "__**Disabled**__"), true)
                    .addField("New:", (newValue ? "__**Enabled**__" : "__**Disabled**__"), true)
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
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
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel topic changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", (oldValue != null ? oldValue : "No Topic Specified"), true)
                    .addField("New:", (newValue != null ? newValue : "No Topic Specified"), true)
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
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
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel name changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", "**" + oldName + "**", true)
                    .addField("New:", "**" + newName + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
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
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**" + Type.valueOf(event.getChannel().getType().toString()).type + " channel slowmode changed: " + event.getChannel().getAsMention() + "**")
                    .addField("Old:", "**" + secondsToSeperatedTime(oldValue) + "**", true)
                    .addField("New:", "**" + secondsToSeperatedTime(newValue) + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Channel ID: " + event.getChannel().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiAdded(EmojiAddedEvent event) {
        RichCustomEmoji emoji = event.getEmoji();
        String emojiFormatted = emoji.getFormatted();

        if (isEnabled("emojiAdded", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**Emoji: " + emojiFormatted + " (" + emoji.getName() + ") has been created**")
                    .setTimestamp(Instant.now())
                    .setFooter("Emoji ID: " + emoji.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiRemoved(EmojiRemovedEvent event) {
        RichCustomEmoji emoji = event.getEmoji();

        if (isEnabled("emojiRemoved", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(15158332)
                    .setDescription("**Emoji: " + emoji.getName() + " has been deleted**")
                    .setTimestamp(Instant.now())
                    .setFooter("Emoji ID: " + emoji.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiUpdateName(EmojiUpdateNameEvent event) {
        RichCustomEmoji emoji = event.getEmoji();
        String emojiFormatted = event.getEmoji().getFormatted();

        String oldName = event.getOldName();
        String newName = event.getNewName();

        if (isEnabled("emojiUpdateName", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("**Emoji: " + emojiFormatted + " (" + emoji.getName() + ") name has been updated**")
                    .addField("Old:", "**" + oldName + "**", true)
                    .addField("New:", "**" + newName + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Emoji ID: " + emoji.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        if (isEnabled("guildBan", event.getGuild().getId())) {
            User bannedUser = event.getUser();

            AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();

            auditLogs.type(ActionType.BAN);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? "N/A" : entries.get(0).getUser().getAsTag();
                String reason = entries.isEmpty() ? "N/A" : entries.get(0).getReason();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor("User banned:", null, bannedUser.getAvatarUrl())
                        .setThumbnail(bannedUser.getAvatarUrl())
                        .setColor(15158332)
                        .setDescription("User: " + bannedUser.getAsTag())
                        .addField("Reason:", reason != null ? reason : "N/A", true)
                        .addField("Banned by:", moderator, true)
                        .setFooter("ID: " + bannedUser.getId())
                        .setTimestamp(Instant.now())
                        .build();

                String logChannelID = getLogChannelID(event.getGuild().getId());

                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
        if (isEnabled("guildUnban", event.getGuild().getId())) {
            User unbannedUser = event.getUser();

            AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();

            auditLogs.type(ActionType.UNBAN);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? "N/A" : entries.get(0).getUser().getAsTag();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor("User unbanned:", null, unbannedUser.getAvatarUrl())
                        .setThumbnail(unbannedUser.getAvatarUrl())
                        .setColor(3066993)
                        .setDescription("User: " + unbannedUser.getAsTag())
                        .addField("Unbanned by:", moderator, true)
                        .setFooter("ID: " + unbannedUser.getId())
                        .setTimestamp(Instant.now())
                        .build();

                String logChannelID = getLogChannelID(event.getGuild().getId());

                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberJoin", event.getGuild().getId())) {
            User member = event.getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor("Member joined:", null, member.getAvatarUrl())
                    .setThumbnail(member.getAvatarUrl())
                    .setColor(3066993)
                    .setDescription(member.getAsMention() + "\nMember count: " + event.getGuild().getMemberCount())
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRemove", event.getGuild().getId())) {
            User member = event.getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor("Member left:", null, member.getAvatarUrl())
                    .setThumbnail(member.getAvatarUrl())
                    .setColor(15158332)
                    .setDescription(member.getAsMention() + "\nMember count: " + event.getGuild().getMemberCount())
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRoleAdd", event.getGuild().getId())) {
            List<Role> roles = event.getRoles();
            StringBuilder addedRoles = new StringBuilder();
            User member = event.getUser();

            if (!roles.isEmpty()) {
                for (Role role : roles) {
                    addedRoles.append(role.getName()).append(", ");
                }

                String roleString = addedRoles.toString();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                        .setColor(3066993)
                        .setDescription("**" + member.getAsTag() + " roles added**")
                        .addField("Added roles", roleString.substring(0, roleString.length() - 2), true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(event.getGuild().getId());

                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRoleRemove", event.getGuild().getId())) {
            List<Role> roles = event.getRoles();
            StringBuilder removedRoles = new StringBuilder();
            User member = event.getUser();

            if (!roles.isEmpty()) {
                for (Role role : roles) {
                    removedRoles.append(role.getName()).append(", ");
                }

                String roleString = removedRoles.toString();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                        .setColor(15158332)
                        .setDescription("**" + member.getAsTag() + " roles removed**")
                        .addField("Removed roles", roleString.substring(0, roleString.length() - 2), true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(event.getGuild().getId());

                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberUpdateNickname", event.getGuild().getId())) {
            Member member = event.getMember();

            String oldNickname = event.getOldNickname();
            String newNickname = event.getNewNickname();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                    .setDescription("**" + member.getUser().getAsTag() + " nickname changed**")
                    .addField("Old:", oldNickname != null ? oldNickname : "None", true)
                    .addField("New:", newNickname != null ? newNickname : "None", true)
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberUpdateTimeOut", event.getGuild().getId())) {
            AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();

            auditLogs.type(ActionType.MEMBER_UPDATE);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? "N/A" : entries.get(0).getUser().getAsTag();
                String reason = entries.isEmpty() ? "N/A" : entries.get(0).getReason();

                User member = event.getUser();
                OffsetDateTime oldTimeout = event.getOldTimeOutEnd();
                OffsetDateTime newTimeout = event.getNewTimeOutEnd();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                        .setColor(15158332)
                        .setDescription("**" + member.getAsTag() + " timeout changed**")
                        .addField("Old Timeout:", oldTimeout != null ? "Until <t:" + oldTimeout.toEpochSecond() + ":F>" : "None", true)
                        .addField("New Timeout:", newTimeout != null ? "Until <t:" + newTimeout.toEpochSecond() + ":F>" : "None", true)
                        .addField("Reason:", reason != null ? reason : "N/A", false)
                        .addField("Timed out by:", moderator, true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(event.getGuild().getId());

                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceDeafen", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isSelfDeafened() ? 15158332 : 3066993)
                    .setDescription("**" + member.getAsTag() + " has been self " + (event.isSelfDeafened() ? "" : "un") + "deafened in " + event.getVoiceState().getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceGuildDeafen", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isGuildDeafened() ? 15158332 : 3066993)
                    .setDescription("**" + member.getAsTag() + " has been server " + (event.isGuildDeafened() ? "" : "un") + "deafened in " + event.getVoiceState().getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceMute", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isSelfMuted() ? 15158332 : 3066993)
                    .setDescription("**" + member.getAsTag() + " has been self " + (event.isSelfMuted() ? "" : "un") + "muted in " + event.getVoiceState().getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceGuildMute", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isGuildMuted() ? 15158332 : 3066993)
                    .setDescription("**" + member.getAsTag() + " has been server " + (event.isGuildMuted() ? "" : "un") + "muted in " + event.getVoiceState().getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (isEnabled("guildVoiceJoin", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(3066993)
                    .setDescription("**" + member.getAsTag() + " joined voice channel " + event.getVoiceState().getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (isEnabled("guildVoiceLeave", event.getGuild().getId())) {

            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(15158332)
                    .setDescription("**" + member.getAsTag() + " left voice channel " + event.getChannelLeft().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().isBot()) return;

        AudioChannel oldChannel = event.getChannelLeft();
        AudioChannel newChannel = event.getChannelJoined();

        if (isEnabled("guildVoiceMove", event.getGuild().getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(16751616)
                    .setDescription("**" + member.getAsTag() + " switched voice channel to" + newChannel.getAsMention() + "**")
                    .addField("Old channel:", oldChannel.getAsMention(), true)
                    .addField("New channel:", newChannel.getAsMention(), true)
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Store Message in cache
        messageMap.put(event.getMessageIdLong(), event.getMessage());
    }


    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;
        if (isEnabled("messageUpdate", event.getGuild().getId())) {
            Member member = event.getMember();

            MessageChannelUnion channel = event.getChannel();

            String oldMessage = messageMap.containsKey(event.getMessageIdLong()) ? messageMap.get(event.getMessageIdLong()).getContentRaw() : "Not in Cache";
            String newMessage = event.getMessage().getContentRaw();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                    .setColor(16751616)
                    .setDescription("**" + member.getUser().getAsTag() + " updated a message in " + channel.getAsMention() + "**")
                    .addField("Old message:", oldMessage.length() > 1024 ? oldMessage.substring(0, 1021) + "..." : oldMessage, true)
                    .addField("New message:", newMessage.length() > 1024 ? newMessage.substring(0, 1021) + "..." : newMessage, true)
                    .setTimestamp(Instant.now())
                    .setFooter("User ID:" + member.getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        // Update MessageCache if present, else add to cache
        if (!messageMap.containsKey(event.getMessageIdLong())) messageMap.put(event.getMessageIdLong(), event.getMessage());
        else messageMap.replace(event.getMessageIdLong(), event.getMessage());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (isEnabled("messageDelete", event.getGuild().getId())) {
            boolean hasMember = messageMap.containsKey(event.getMessageIdLong());

            Message message = messageMap.getOrDefault(event.getMessageIdLong(), null);
            Member member = hasMember ? event.getGuild().getMemberById(message.getAuthor().getId()) : null;

            if (member != null && member.getUser().isBot()) return;

            MessageChannelUnion channel = event.getChannel();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member != null ? member.getUser().getAsTag() : "Unknown User", null, member != null ? member.getUser().getAvatarUrl() : null)
                    .setColor(15158332)
                    .setDescription("**" + (member != null ? member.getUser().getAsTag() : "An Unknown User") + " deleted a message in " + channel.getAsMention() + "**")
                    .addField("Message:", message != null ? !message.getContentRaw().equals("") ? message.getContentRaw().length() > 1024 ? message.getContentRaw().substring(0, 1021) + "..." : message.getContentRaw() : "The message had no content." : "The message was not in cache. This is probably due to the message being an old one.", true)
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + (member != null ? member.getId() : "N/A"))
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        messageMap.remove(event.getMessageIdLong());
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        if (isEnabled("messageBulkDelete", event.getGuild().getId())) {
            List<String> messageIds = new ArrayList<>(event.getMessageIds());

            Collections.reverse(messageIds);

            StringBuilder sb = new StringBuilder();

            for (String msgId : messageIds) {
                Message message = messageMap.getOrDefault(Long.parseLong(msgId), null);

                if (message != null && message.getAuthor().isBot()) return;

                LocalDateTime date = message != null ? LocalDateTime.ofInstant(message.getTimeCreated().toInstant(), ZoneId.of("Europe/Paris")) : null;

                sb
                        .append(date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "??")
                        .append(" - ")
                        .append(message != null ? event.getGuild().getMemberById(message.getAuthor().getId()).getUser().getAsTag() : "N/A")
                        .append(" - ")
                        .append(message != null ? !message.getContentRaw().equals("") ? message.getContentRaw() : "No Message Content" : "N/A")
                        .append("\n");

                messageMap.remove(Long.parseLong(msgId));
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(15158332)
                    .setDescription("**" + messageIds.size() + " were deleted a message in " + event.getChannel().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            FileUpload file = AttachedFile.fromData(sb.toString().getBytes(), "messageLog.txt");

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).addFiles(file).queue();
        }
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        if (isEnabled("roleCreate", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role created: " + event.getRole().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (isEnabled("roleDelete", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(15158332)
                    .setDescription("** Role deleted: " + event.getRole().getName() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {
        if (isEnabled("roleUpdateColor", event.getGuild().getId())) {
            Color oldColor = event.getOldColor();
            Color newColor = event.getNewColor();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(newColor != null ? newColor.getRGB() : 3066993)
                    .setDescription("** Role color changed: " + event.getRole().getName() + "**")
                    .addField("Old Color:", oldColor != null ? String.format("#%02x%02x%02x", oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue()) : "Default", true)
                    .addField("New Color:", newColor != null ? String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue()) : "Default", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateHoisted(RoleUpdateHoistedEvent event) {
        if (isEnabled("roleUpdateHoisted", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role has been " + (event.wasHoisted() ? "un-" : "") + "hoisted :" + event.getRole().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateIcon(RoleUpdateIconEvent event) {
        if (isEnabled("roleUpdateIcon", event.getGuild().getId())) {
            RoleIcon oldIcon = event.getOldIcon();
            RoleIcon newIcon = event.getNewIcon();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role icon changed: " + event.getRole().getName() + "**")
                    .addField("Old Icon:", oldIcon != null ? oldIcon.getIconUrl() != null ? oldIcon.getIconUrl() : "No Role Icon" : "No Role Icon", true)
                    .addField("New Icon:", newIcon != null ? newIcon.getIconUrl() != null ? newIcon.getIconUrl() : "No Role Icon" : "No Role Icon", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateMentionable(RoleUpdateMentionableEvent event) {
        if (isEnabled("roleUpdateMentionable", event.getGuild().getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role is now " + (event.wasMentionable() ? "un-" : "") + "mentionable :" + event.getRole().getAsMention() + "**")
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        if (isEnabled("roleUpdateName", event.getGuild().getId())) {
            String oldName = event.getOldName();
            String newName = event.getNewName();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role name changed: " + event.getRole().getAsMention() + "**")
                    .addField("Old Name:", oldName, true)
                    .addField("New Name:", newName, true)
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
                    .build();

            String logChannelID = getLogChannelID(event.getGuild().getId());

            assert logChannelID != null;
            TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        if (isEnabled("roleUpdatePermissions", event.getGuild().getId())) {
            EnumSet<Permission> oldPermissions = event.getOldPermissions();
            EnumSet<Permission> newPermissions = event.getNewPermissions();

            StringBuilder addedSB = new StringBuilder();
            StringBuilder removedSB = new StringBuilder();

            for (Permission permission : newPermissions) {
                if (!oldPermissions.contains(permission)) addedSB.append(permission.getName()).append(", ");
            }

            for (Permission permission : oldPermissions) {
                if (!newPermissions.contains(permission)) removedSB.append(permission.getName()).append(", ");
            }

            String addedString = addedSB.toString();
            String removedString = removedSB.toString();


            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setColor(3066993)
                    .setDescription("** Role permissions changed: " + event.getRole().getAsMention() + "**")
                    .addField("Added:", !addedString.equals("") ? addedString.substring(0, addedString.length() - 2) : "N/A", true)
                    .addField("Removed:", !removedString.equals("") ? removedString.substring(0, removedString.length() - 2) : "N/A", true)
                    .setTimestamp(Instant.now())
                    .setFooter("Role ID: " + event.getRole().getId())
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
