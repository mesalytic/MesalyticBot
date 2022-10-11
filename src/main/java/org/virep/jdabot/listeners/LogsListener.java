package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateHoistedEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateIconEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateMentionableEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import org.virep.jdabot.language.Language;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.virep.jdabot.utils.DatabaseUtils.getLogChannelID;
import static org.virep.jdabot.utils.DatabaseUtils.isEnabled;
import static org.virep.jdabot.utils.Utils.secondsToSeperatedTime;

public class LogsListener extends ListenerAdapter {

    // Map<messageID, messageContent>
    private final Map<Long, Message> messageMap = new HashMap<>();

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        Guild guild = event.getGuild();
        ChannelType type = event.getChannelType();

        if (type == ChannelType.PRIVATE || type == ChannelType.UNKNOWN) return;

        if (isEnabled("channelCreate", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELCREATE", guild).replace("%TYPE%", Language.getLanguage(guild).equals("en") ? Type.valueOf(type.toString()).typeEN : Type.valueOf(type.toString()).typeFR).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();


            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        Guild guild = event.getGuild();
        ChannelType type = event.getChannelType();

        if (type == ChannelType.PRIVATE || type == ChannelType.UNKNOWN) return;

        if (isEnabled("channelDelete", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELDELETE", guild).replace("%CHANNELNAME%", event.getChannel().getName()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();


            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelUpdateNSFW(ChannelUpdateNSFWEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("channelNSFWUpdate", guild.getId())) {
            boolean oldValue = event.getOldValue();
            boolean newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELNSFW", guild).replace("%TYPE%", Language.getLanguage(guild).equals("en") ? Type.valueOf(event.getChannel().getType().toString()).typeEN : Type.valueOf(event.getChannel().getType().toString()).typeFR).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), (oldValue ? Language.getString("LOGSEVENT_CHANNELNSFW_ENABLED", guild) : Language.getString("LOGSEVENT_CHANNELNSFW_DISABLED", guild)), true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), (newValue ? Language.getString("LOGSEVENT_CHANNELNSFW_ENABLED", guild) : Language.getString("LOGSEVENT_CHANNELNSFW_DISABLED", guild)), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelUpdateTopic(ChannelUpdateTopicEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("channelTopicUpdate", guild.getId())) {
            String oldValue = event.getOldValue();
            String newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELTOPIC", guild).replace("%TYPE%", Language.getLanguage(guild).equals("en") ? Type.valueOf(event.getChannel().getType().toString()).typeEN : Type.valueOf(event.getChannel().getType().toString()).typeFR).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), (oldValue != null ? oldValue : Language.getString("LOGSEVENT_CHANNELTOPIC_NOTOPIC", guild)), true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), (newValue != null ? newValue : Language.getString("LOGSEVENT_CHANNELTOPIC_NOTOPIC", guild)), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("channelNameUpdate", guild.getId())) {
            String oldName = event.getOldValue();
            String newName = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELNAME", guild).replace("%TYPE%", Language.getLanguage(guild).equals("en") ? Type.valueOf(event.getChannel().getType().toString()).typeEN : Type.valueOf(event.getChannel().getType().toString()).typeFR).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), "**" + oldName + "**", true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), "**" + newName + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onChannelUpdateSlowmode(ChannelUpdateSlowmodeEvent event) {
        Guild guild = event.getGuild();
        if (isEnabled("channelSlowmodeUpdate", guild.getId())) {
            int oldValue = event.getOldValue();
            int newValue = event.getNewValue();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_CHANNELSLOWMODE", guild).replace("%TYPE%", Language.getLanguage(guild).equals("en") ? Type.valueOf(event.getChannel().getType().toString()).typeEN : Type.valueOf(event.getChannel().getType().toString()).typeFR).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), "**" + secondsToSeperatedTime(oldValue) + "**", true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), "**" + secondsToSeperatedTime(newValue) + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_CHANNELID", guild).replace("%CHANNELID%", event.getChannel().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiAdded(EmojiAddedEvent event) {
        RichCustomEmoji emoji = event.getEmoji();
        String emojiFormatted = emoji.getFormatted();

        Guild guild = event.getGuild();

        if (isEnabled("emojiAdded", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_EMOJIADD", guild).replace("%EMOJI%", emojiFormatted).replace("%EMOJINAME%", emoji.getName()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_EMOJIID", guild).replace("%EMOJIID%", emoji.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiRemoved(EmojiRemovedEvent event) {
        Guild guild = event.getGuild();
        RichCustomEmoji emoji = event.getEmoji();

        if (isEnabled("emojiRemoved", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_EMOJIDELETE", guild).replace("%EMOJINAME%", emoji.getName()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_EMOJIID", guild).replace("%EMOJIID%", emoji.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onEmojiUpdateName(EmojiUpdateNameEvent event) {
        Guild guild = event.getGuild();

        RichCustomEmoji emoji = event.getEmoji();
        String emojiFormatted = event.getEmoji().getFormatted();

        String oldName = event.getOldName();
        String newName = event.getNewName();

        if (isEnabled("emojiUpdateName", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_EMOJIUPDATE", guild).replace("%EMOJI%", emojiFormatted).replace("%EMOJINAME%", emoji.getName()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), "**" + oldName + "**", true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), "**" + newName + "**", true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_EMOJIID", guild).replace("%EMOJIID%", emoji.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("guildBan", guild.getId())) {
            User bannedUser = event.getUser();

            AuditLogPaginationAction auditLogs = guild.retrieveAuditLogs();

            auditLogs.type(ActionType.BAN);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? Language.getString("LOGSEVENT_NA", guild) : entries.get(0).getUser().getAsTag();
                String reason = entries.isEmpty() ? Language.getString("LOGSEVENT_NA", guild) : entries.get(0).getReason();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(Language.getString("LOGSEVENT_BAN_USERBANNED", guild), null, bannedUser.getAvatarUrl())
                        .setThumbnail(bannedUser.getAvatarUrl())
                        .setColor(15158332)
                        .setDescription(Language.getString("LOGSEVENT_BAN", guild).replace("%USERTAG%", bannedUser.getAsTag()))
                        .addField(Language.getString("LOGSEVENT_REASON", guild), reason != null ? reason : Language.getString("LOGSEVENT_NA", guild), true)
                        .addField(Language.getString("LOGSEVENT_BAN_BANNEDBY", guild), moderator, true)
                        .setFooter("ID: " + bannedUser.getId())
                        .setTimestamp(Instant.now())
                        .build();

                String logChannelID = getLogChannelID(guild.getId());

                assert logChannelID != null;
                TextChannel logChannel = guild.getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("guildUnban", guild.getId())) {
            User unbannedUser = event.getUser();

            AuditLogPaginationAction auditLogs = guild.retrieveAuditLogs();

            auditLogs.type(ActionType.UNBAN);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? Language.getString("LOGSEVENT_NA", guild) : entries.get(0).getUser().getAsTag();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(Language.getString("LOGSEVENT_UNBAN_USERUNBANNED", guild), null, unbannedUser.getAvatarUrl())
                        .setThumbnail(unbannedUser.getAvatarUrl())
                        .setColor(3066993)
                        .setDescription(Language.getString("LOGSEVENT_UNBAN", guild))
                        .addField(Language.getString("LOGSEVENT_UNBAN_UNBANNEDBY", guild), moderator, true)
                        .setFooter("ID: " + unbannedUser.getId())
                        .setTimestamp(Instant.now())
                        .build();

                String logChannelID = getLogChannelID(guild.getId());

                assert logChannelID != null;
                TextChannel logChannel = guild.getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberJoin", guild.getId())) {
            User member = event.getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(Language.getString("LOGSEVENT_MEMBERJOIN_JOINED", guild), null, member.getAvatarUrl())
                    .setThumbnail(member.getAvatarUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_MEMBERCOUNT", guild).replace("%USERMENTION%", member.getAsMention()).replace("%MEMBERCOUNT%", String.valueOf(guild.getMemberCount())))
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRemove", guild.getId())) {
            User member = event.getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(Language.getString("LOGSEVENT_MEMBERREMOVE_LEFT", guild), null, member.getAvatarUrl())
                    .setThumbnail(member.getAvatarUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_MEMBERCOUNT", guild).replace("%USERMENTION%", member.getAsMention()).replace("%MEMBERCOUNT%", String.valueOf(guild.getMemberCount())))
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRoleAdd", guild.getId())) {
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
                        .setDescription(Language.getString("LOGSEVENT_ROLEADD", guild).replace("%MEMBERTAG%", member.getAsTag()))
                        .addField(Language.getString("LOGSEVENT_ROLEADD_ADDED", guild), roleString.substring(0, roleString.length() - 2), true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(guild.getId());

                assert logChannelID != null;
                TextChannel logChannel = guild.getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberRoleRemove", guild.getId())) {
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
                        .setDescription(Language.getString("LOGSEVENT_ROLEREMOVE", guild).replace("%MEMBERTAG%", member.getAsTag()))
                        .addField(Language.getString("LOGSEVENT_ROLEREMOVE_REMOVED", guild), roleString.substring(0, roleString.length() - 2), true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(guild.getId());

                assert logChannelID != null;
                TextChannel logChannel = guild.getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberUpdateNickname", guild.getId())) {
            Member member = event.getMember();

            String oldNickname = event.getOldNickname();
            String newNickname = event.getNewNickname();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                    .setDescription(Language.getString("LOGSEVENT_NICKNAME", guild).replace("%USERTAG%", member.getUser().getAsTag()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), oldNickname != null ? oldNickname : Language.getString("LOGSEVENT_NA", guild), true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), newNickname != null ? newNickname : Language.getString("LOGSEVENT_NA", guild), true)
                    .setTimestamp(Instant.now())
                    .setFooter("ID: " + member.getId())
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        Guild guild = event.getGuild();

        if (event.getUser().isBot()) return;
        if (isEnabled("guildMemberUpdateTimeOut", guild.getId())) {
            AuditLogPaginationAction auditLogs = guild.retrieveAuditLogs();

            auditLogs.type(ActionType.MEMBER_UPDATE);
            auditLogs.limit(1);

            auditLogs.queue((entries) -> {
                String moderator = entries.isEmpty() ? Language.getString("LOGSEVENT_NA", guild) : entries.get(0).getUser().getAsTag();
                String reason = entries.isEmpty() ? Language.getString("LOGSEVENT_NA", guild) : entries.get(0).getReason();

                User member = event.getUser();
                OffsetDateTime oldTimeout = event.getOldTimeOutEnd();
                OffsetDateTime newTimeout = event.getNewTimeOutEnd();

                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                        .setColor(15158332)
                        .setDescription(Language.getString("LOGSEVENT_TIMEOUT", guild).replace("%USERTAG%", member.getAsTag()))
                        .addField(Language.getString("LOGSEVENT_OLD", guild), oldTimeout != null ? Language.getString("LOGSEVENT_TIMEOUT_UNTIL", guild).replace("%SECONDS%", String.valueOf(oldTimeout.toEpochSecond())) : Language.getString("LOGSEVENT_NA", guild), true)
                        .addField(Language.getString("LOGSEVENT_NEW", guild), newTimeout != null ? Language.getString("LOGSEVENT_TIMEOUT_UNTIL", guild).replace("%SECONDS%", String.valueOf(newTimeout.toEpochSecond())) : Language.getString("LOGSEVENT_NA", guild), true)
                        .addField(Language.getString("LOGSEVENT_REASON", guild), reason != null ? reason : Language.getString("LOGSEVENT_NA", guild), false)
                        .addField(Language.getString("LOGSEVENT_TIMEOUT_TIMEDOUTBY", guild), moderator, true)
                        .setTimestamp(Instant.now())
                        .setFooter("ID: " + member.getId())
                        .build();

                String logChannelID = getLogChannelID(guild.getId());

                assert logChannelID != null;
                TextChannel logChannel = guild.getTextChannelById(logChannelID);

                if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
            });
        }
    }

    @Override
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
        Guild guild = event.getGuild();

        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceDeafen", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isSelfDeafened() ? 15158332 : 3066993)
                    .setDescription(Language.getString("LOGSEVENT_SELFDEAF", guild).replace("%MEMBERTAG%", member.getAsTag()).replace("%STATUS%", (event.isSelfDeafened() ? Language.getString("LOGSEVENT_DEAFENED", guild) : Language.getString("LOGSEVENT_UNDEAFENED", guild))).replace("%CHANNELMENTION%", event.getVoiceState().getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        Guild guild = event.getGuild();

        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceGuildDeafen", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isGuildDeafened() ? 15158332 : 3066993)
                    .setDescription(Language.getString("LOGSEVENT_SERVERDEAF", guild).replace("%MEMBERTAG%", member.getAsTag()).replace("%STATUS%", (event.isGuildDeafened() ? Language.getString("LOGSEVENT_DEAFENED", guild) : Language.getString("LOGSEVENT_UNDEAFENED", guild))).replace("%CHANNELMENTION%", event.getVoiceState().getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
        Guild guild = event.getGuild();

        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceMute", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isSelfMuted() ? 15158332 : 3066993)
                    .setDescription(Language.getString("LOGSEVENT_SELFMUTE", guild).replace("%MEMBERTAG%", member.getAsTag()).replace("%STATUS%", (event.isSelfMuted() ? Language.getString("LOGSEVENT_MUTED", guild) : Language.getString("LOGSEVENT_UNMUTED", guild))).replace("%CHANNELMENTION%", event.getVoiceState().getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        Guild guild = event.getGuild();
        if (event.getMember().getUser().isBot()) return;
        if (!event.getVoiceState().inAudioChannel()) return;

        if (isEnabled("guildVoiceGuildMute", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(event.isGuildMuted() ? 15158332 : 3066993)
                    .setDescription(Language.getString("LOGSEVENT_SERVERMUTE", guild).replace("%MEMBERTAG%", member.getAsTag()).replace("%STATUS%", (event.isGuildMuted() ? Language.getString("LOGSEVENT_MUTED", guild) : Language.getString("LOGSEVENT_UNMUTED", guild))).replace("%CHANNELMENTION%", event.getVoiceState().getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) return;

        Guild guild = event.getGuild();

        AudioChannelUnion channelJoined = event.getChannelJoined();
        AudioChannelUnion channelLeft = event.getChannelLeft();

        if (channelLeft == null && channelJoined != null && isEnabled("guildVoiceJoin", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_VCJOIN", guild).replace("%USERTAG%", member.getAsTag()).replace("%CHANNELMENTION%", channelJoined.getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        if (channelLeft != null && channelJoined == null && isEnabled("guildVoiceLeave", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_VCLEFT", guild).replace("%USERTAG%", member.getAsTag()).replace("%CHANNELMENTION%", channelLeft.getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        if (channelLeft != null && channelJoined != null && isEnabled("guildVoiceMove", guild.getId())) {
            User member = event.getMember().getUser();
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getAsTag(), null, member.getAvatarUrl())
                    .setColor(16751616)
                    .setDescription(Language.getString("LOGSEVENT_VCMOVE", guild).replace("%USERTAG%", member.getAsTag()).replace("%CHANNELMENTION%", channelJoined.getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), channelLeft.getAsMention(), true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), channelJoined.getAsMention(), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

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
        Guild guild = event.getGuild();

        if (event.getAuthor().isBot()) return;
        if (isEnabled("messageUpdate", guild.getId())) {
            Member member = event.getMember();

            MessageChannelUnion channel = event.getChannel();

            String oldMessage = messageMap.containsKey(event.getMessageIdLong()) ? messageMap.get(event.getMessageIdLong()).getContentRaw() : "Not in Cache";
            String newMessage = event.getMessage().getContentRaw();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                    .setColor(16751616)
                    .setDescription(Language.getString("LOGSEVENT_MSGUPDATE", guild).replace("%USERTAG%", member.getUser().getAsTag()).replace("%CHANNELMENTION%", channel.getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), oldMessage.length() > 1024 ? oldMessage.substring(0, 1021) + "..." : oldMessage, true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), newMessage.length() > 1024 ? newMessage.substring(0, 1021) + "..." : newMessage, true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID", member.getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        // Update MessageCache if present, else add to cache
        if (!messageMap.containsKey(event.getMessageIdLong()))
            messageMap.put(event.getMessageIdLong(), event.getMessage());
        else messageMap.replace(event.getMessageIdLong(), event.getMessage());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("messageDelete", guild.getId())) {
            boolean hasMember = messageMap.containsKey(event.getMessageIdLong());

            Message message = messageMap.getOrDefault(event.getMessageIdLong(), null);
            Member member = hasMember ? guild.getMemberById(message.getAuthor().getId()) : null;

            if (member != null && member.getUser().isBot()) return;

            MessageChannelUnion channel = event.getChannel();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(member != null ? member.getUser().getAsTag() : Language.getString("LOGSEVENT_UNKNOWN", guild), null, member != null ? member.getUser().getAvatarUrl() : null)
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_MSGDELETE", guild).replace("%USERTAG%", (member != null ? member.getUser().getAsTag() : Language.getString("LOGSEVENT_UNKNOWN", guild))).replace("%CHANNELMENTION%", channel.getAsMention()))
                    .addField("Message:", message != null ? !message.getContentRaw().equals("") ? message.getContentRaw().length() > 1024 ? message.getContentRaw().substring(0, 1021) + "..." : message.getContentRaw() : Language.getString("LOGSEVENT_MSGDELETE_NOCONTENT", guild) : Language.getString("LOGSEVENT_MSGDELETE_NOCACHE", guild), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_USERID", guild).replace("%USERID%", (member != null ? member.getId() : Language.getString("LOGSEVENT_NA", guild))))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }

        messageMap.remove(event.getMessageIdLong());
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("messageBulkDelete", guild.getId())) {
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
                        .append(message != null ? guild.getMemberById(message.getAuthor().getId()).getUser().getAsTag() : Language.getString("LOGSEVENT_NA", guild))
                        .append(" - ")
                        .append(message != null ? !message.getContentRaw().equals("") ? message.getContentRaw() : Language.getString("LOGSEVENT_MSGBULK_NOCONTENT", guild) : Language.getString("LOGSEVENT_NA", guild))
                        .append("\n");

                messageMap.remove(Long.parseLong(msgId));
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_MSGBULK", guild).replace("%SIZE%", String.valueOf(messageIds.size())).replace("%CHANNELMENTION%", event.getChannel().getAsMention()))
                    .setTimestamp(Instant.now())
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            FileUpload file = AttachedFile.fromData(sb.toString().getBytes(), "messageLog.txt");

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).addFiles(file).queue();
        }
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleCreate", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLECREATE", guild).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleDelete", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(15158332)
                    .setDescription(Language.getString("LOGSEVENT_ROLEDELETE", guild).replace("%ROLENAME%", event.getRole().getName()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleUpdateColor", guild.getId())) {
            Color oldColor = event.getOldColor();
            Color newColor = event.getNewColor();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(newColor != null ? newColor.getRGB() : 3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLECOLOR", guild).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), oldColor != null ? String.format("#%02x%02x%02x", oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue()) : "Default", true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), newColor != null ? String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue()) : "Default", true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateHoisted(RoleUpdateHoistedEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleUpdateHoisted", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLEHOIST", guild).replace("%STATUS%", (event.wasHoisted() ? Language.getString("LOGSEVENT_UNHOISTED", guild) : Language.getString("LOGSEVENT_HOISTED", guild))).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateIcon(RoleUpdateIconEvent event) {
        Guild guild = event.getGuild();
        if (isEnabled("roleUpdateIcon", guild.getId())) {
            RoleIcon oldIcon = event.getOldIcon();
            RoleIcon newIcon = event.getNewIcon();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLEICON", guild).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), oldIcon != null ? oldIcon.getIconUrl() != null ? oldIcon.getIconUrl() : Language.getString("LOGSEVENT_NOICON", guild) : Language.getString("LOGSEVENT_NOICON", guild), true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), newIcon != null ? newIcon.getIconUrl() != null ? newIcon.getIconUrl() : Language.getString("LOGSEVENT_NOICON", guild) : Language.getString("LOGSEVENT_NOICON", guild), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateMentionable(RoleUpdateMentionableEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleUpdateMentionable", guild.getId())) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLEMENTION", guild).replace("%STATUS%", (event.wasMentionable() ? Language.getString("LOGSEVENT_UNMENTION", guild) : Language.getString("LOGSEVENT_MENTION", guild)).replace("%ROLEMENTION%", event.getRole().getAsMention())))
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleUpdateName", guild.getId())) {
            String oldName = event.getOldName();
            String newName = event.getNewName();

            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLENAME", guild).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_OLD", guild), oldName, true)
                    .addField(Language.getString("LOGSEVENT_NEW", guild), newName, true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        Guild guild = event.getGuild();

        if (isEnabled("roleUpdatePermissions", guild.getId())) {
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
                    .setAuthor(guild.getName(), null, guild.getIconUrl())
                    .setColor(3066993)
                    .setDescription(Language.getString("LOGSEVENT_ROLEPERMS", guild).replace("%ROLEMENTION%", event.getRole().getAsMention()))
                    .addField(Language.getString("LOGSEVENT_ROLEPERMS_ADDED", guild), !addedString.equals("") ? addedString.substring(0, addedString.length() - 2) : Language.getString("LOGSEVENT_NA", guild), true)
                    .addField(Language.getString("LOGSEVENT_ROLEPERMS_REMOVED", guild), !removedString.equals("") ? removedString.substring(0, removedString.length() - 2) : Language.getString("LOGSEVENT_NA", guild), true)
                    .setTimestamp(Instant.now())
                    .setFooter(Language.getString("LOGSEVENT_ROLEID", guild).replace("%ROLEID%", event.getRole().getId()))
                    .build();

            String logChannelID = getLogChannelID(guild.getId());

            assert logChannelID != null;
            TextChannel logChannel = guild.getTextChannelById(logChannelID);

            if (logChannel != null) logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    enum Type {
        CATEGORY("Category", "Catégorie"),
        GUILD_NEWS_THREAD("News Thread", "Thread d'information"),
        GUILD_PRIVATE_THREAD("Private Thread", "Thread privé"),
        GUILD_PUBLIC_THREAD("Public Thread", "Thread public"),
        NEWS("News", "Informations"),
        STAGE("Stage", "Espace"),
        TEXT("Text", "Textuel"),
        VOICE("Voice", "Vocal");

        public final String typeEN;
        public final String typeFR;

        Type(String typeEN, String typeFR) {
            this.typeEN = typeEN;
            this.typeFR = typeFR;
        }
    }
}
