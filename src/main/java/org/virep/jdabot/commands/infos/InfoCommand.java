package org.virep.jdabot.commands.infos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.badgesToEmote;

public class InfoCommand implements Command {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays info about a user or the server.")
                .addSubcommands(
                        new SubcommandData("user", "Info about a user.")
                                .addOption(OptionType.USER, "user", "The user you want to see info about."),
                        new SubcommandData("server", "Info about the server")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
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
                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                    .setThumbnail(member.getUser().getAvatarUrl())
                    .addField("ID", member.getId(), true)
                    .addField("Type", member.getUser().isBot() ? "Bot" : "User", true)
                    .addField("Nickname", member.getNickname() != null ? member.getNickname() : "N/A", true)
                    .addField("Nitro Boost Status", member.isBoosting() ? "<t:" + Objects.requireNonNull(member.getTimeBoosted()).toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeBoosted().toInstant().getEpochSecond() + ":R>)" : "None", true)
                    .addField("Account Created", "<t:" + member.getTimeCreated().toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeCreated().toInstant().getEpochSecond() + ":R>)", false)
                    .addField("Joined server", "<t:" + member.getTimeJoined().toInstant().getEpochSecond() + ":F> (<t:" + member.getTimeJoined().toInstant().getEpochSecond() + ":R>)", false)
                    .addField("Badges", flags.isEmpty() ? "None" : badgesToEmote(flagString), false)
                    .addField("Roles [" + roles.size() + "]", !roles.isEmpty() ? roleString.length() > 1024 ? roleString.substring(0, 1017) + "[...]" : roleString : "None", false)
                    .build();

            event.replyEmbeds(embed).queue();
        } else {
            Guild guild = event.getGuild();
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
                    .addField("Server ID", guild.getId(), true)
                    .addField("Owner", Objects.requireNonNull(guild.getOwner()).getUser().getAsTag(), true)
                    .addField("Member Count", String.valueOf(guild.getMemberCount()), true)
                    .addField("Boosts", guild.getBoostCount() + " boosts (Level " + guild.getBoostTier().getKey() + ")", true)
                    .addField("Preferred Locale", guild.getLocale().getLanguageName(), true)
                    .addField("Channel Count", String.valueOf(guild.getChannels().size()), true)
                    .addField("Joined on", "<t:" + Objects.requireNonNull(event.getMember()).getTimeJoined().toInstant().getEpochSecond() + ":F> (<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":R>", false)
                    .addField("Created On", "<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":F> (<t:" + guild.getTimeCreated().toInstant().getEpochSecond() + ":R>)", false)
                    .addField("Roles [" + roles.size() + "]", !roles.isEmpty() ? roleString.length() > 1024 ? roleString.substring(0, 1017) + "[...]" : roleString : "None", false)
                    .addField("Emojis [" + guild.getEmojis().size() + "]", emojiString + "[...]", false)
                    .build();

            event.replyEmbeds(embed).queue();
        }
    }
}
