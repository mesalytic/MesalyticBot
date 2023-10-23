package org.virep.jdabot.commands.infos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.badgesToEmote;

public class InfoCommand implements SlashCommand {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays info about a user or the server.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche des informations à propos d'un membre ou du serveur.")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("user", "Info about a user.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche les informations à propos d'un membre.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to see info about.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre auquel vous voulez avoir des informations.")
                                ),
                        new SubcommandData("server", "Info about the server")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche les informations à propos du serveur.")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MESSAGE_EMBED_LINKS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (Objects.equals(event.getSubcommandName(), "user")) {
            OptionMapping memberOption = event.getOption("user");
            Member member = memberOption != null ? memberOption.getAsMember() : event.getMember();

            assert member != null;

            EnumSet<User.UserFlag> flags = member.getUser().getFlags();
            List<Role> roles = member.getRoles();

            StringBuilder flagStringBuilder = new StringBuilder();
            StringBuilder roleStringBuilder = new StringBuilder();

            if (member.isBoosting()) flagStringBuilder.append("boost");
            flags.forEach(flag -> flagStringBuilder.append(flag.getName()));

            roles.forEach(role -> roleStringBuilder.append(role.getAsMention()).append(" "));

            String flagString = flagStringBuilder.toString();
            String roleString = roleStringBuilder.toString();

            MessageEmbed embed = new EmbedBuilder()
                    .setColor(member.getColor() != null ? member.getColor() : null)
                    .setAuthor(member.getUser().getEffectiveName(), null, member.getUser().getAvatarUrl())
                    .setThumbnail(member.getUser().getAvatarUrl())
                    .addField("ID", member.getId(), true)
                    .addField("Type", member.getUser().isBot() ? "Bot" : Language.getString("INFO_USEREMBED_TYPE_USER", guild), true)
                    .addField(Language.getString("INFO_USEREMBED_NICKNAME", guild), member.getNickname() != null ? member.getNickname() : Language.getString("INFO_NONE", guild), true)
                    .addField(Language.getString("INFO_USEREMBED_NITRO", guild), member.isBoosting() ? "<t:" + Objects.requireNonNull(member.getTimeBoosted()).toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeBoosted().toInstant().getEpochSecond() + ":R>)" : Language.getString("INFO_NONE", guild), true)
                    .addField(Language.getString("INFO_USEREMBED_CREATED", guild), "<t:" + member.getTimeCreated().toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeCreated().toInstant().getEpochSecond() + ":R>)", false)
                    .addField(Language.getString("INFO_USEREMBED_JOINED", guild), "<t:" + member.getTimeJoined().toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeJoined().toInstant().getEpochSecond() + ":R>)", false)
                    .addField("Badges", flags.isEmpty() ? Language.getString("INFO_NONE", guild) : badgesToEmote(flagString), false)
                    .addField("Roles [" + roles.size() + "]", !roles.isEmpty() ? roleString.length() > 1024 ? roleString.substring(0, 1017) + "[...]" : roleString : Language.getString("INFO_NONE", guild), false)
                    .build();

            event.replyEmbeds(embed).queue();
        } else {

            assert guild != null;

            List<Role> roles = guild.getRoles();
            List<RichCustomEmoji> emojis = guild.getEmojis().stream().limit(20).toList();

            StringBuilder roleStringBuilder = new StringBuilder();
            StringBuilder emojisStringBuilder = new StringBuilder();

            roles.forEach(role -> roleStringBuilder.append(role.getAsMention()).append(" "));

            for (RichCustomEmoji emoji : emojis) {
                emojisStringBuilder.append(emoji.getFormatted()).append(" ");
            }

            String roleString = roleStringBuilder.toString();
            String emojiString = emojisStringBuilder.toString();

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(guild.getName())
                    .setImage(guild.getBannerUrl())
                    .setThumbnail(guild.getIconUrl())
                    .addField(Language.getString("INFO_SERVEREMBED_SERVERID", guild), guild.getId(), true)
                    .addField(Language.getString("INFO_SERVEREMBED_OWNER", guild), Objects.requireNonNull(guild.getOwner()).getUser().getEffectiveName(), true)
                    .addField(Language.getString("INFO_SERVEREMBED_MEMBERCOUNT", guild), String.valueOf(guild.getMemberCount()), true)
                    .addField("Boosts", guild.getBoostCount() + " boosts (Level " + guild.getBoostTier().getKey() + ")", true)
                    .addField(Language.getString("INFO_SERVEREMBED_COUNT", guild), guild.getLocale().getLanguageName(), true)
                    .addField(Language.getString("INFO_SERVEREMBED_CHANNELCOUNT", guild), String.valueOf(guild.getChannels().size()), true)
                    .addField(Language.getString("INFO_SERVEREMBED_JOINED", guild), "<t:" + Objects.requireNonNull(event.getMember()).getTimeJoined().toInstant().getEpochSecond() + ":F> (<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":R>", false)
                    .addField(Language.getString("INFO_SERVEREMBED_CREATED", guild), "<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":F> (<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":R>)", false)
                    .addField("Roles [" + roles.size() + "]", !roles.isEmpty() ? roleString.length() > 1024 ? roleString.substring(0, 1017) + "[...]" : roleString : Language.getString("INFO_NONE", guild), false)
                    .addField("Emojis [" + guild.getEmojis().size() + "]", emojiString + "[...]", false)
                    .build();

            event.replyEmbeds(embed).queue();
        }
    }
}
