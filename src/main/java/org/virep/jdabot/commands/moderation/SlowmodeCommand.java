package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.secondsToSeperatedTime;

public class SlowmodeCommand implements Command {
    @Override
    public String getName() {
        return "slowmode";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Configure the slowmode for a specific channel.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addSubcommands(
                        new SubcommandData("set", "Configure the slowmode duration.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure la durée du slowmode.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "duration", "The slowmode duration. Default is 0 seconds.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La durée du slowmode. (defaut: 0 secondes)"),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel to configure. Default is interaction channel.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon a mettre sous slowmode. (defaut: ce salon)")
                                ),
                        new SubcommandData("off", "Disables the slowmode for that specific channel.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Désactive le slowmode.")
                                .addOptions(
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel that has the slowmode.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon qui est sous slowmode.")
                                )
                );

    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        OptionMapping channelMapping = event.getOption("channel");
        assert channelMapping != null;

        GuildChannelUnion channel = channelMapping.getAsChannel();

        if (channel.getType() != ChannelType.TEXT) {
            event.reply(Language.getString("SLOWMODE_NOTTEXTCHANNEL", guild)).setEphemeral(true).queue();
            return;
        }

        if (Objects.equals(event.getSubcommandName(), "set")) {
            OptionMapping durationMapping = event.getOption("duration");
            assert durationMapping != null;

            try {
                int slowmodeSecs = (int) Duration.parse("PT" + durationMapping.getAsString().toUpperCase().replace(" ", "")).getSeconds();

                if (slowmodeSecs > TextChannel.MAX_SLOWMODE || slowmodeSecs < 0) {
                    event.reply(Language.getString("SLOWMODE_WRONGDURATION", guild)).setEphemeral(true).queue();
                    return;
                }

                channel.asTextChannel().getManager().setSlowmode(slowmodeSecs).queue();

                event.reply(Language.getString("SLOWMODE_SET", guild).replace("%CHANNELMENTION%", channel.getAsMention()).replace("%DURATION%", secondsToSeperatedTime(slowmodeSecs))).queue();
            } catch (DateTimeParseException e) {
                event.reply(Language.getString("SLOWMODE_WRONGDURATION", guild)).setEphemeral(true).queue();
            }

        } else {
            int slowmode = channel.asTextChannel().getSlowmode();

            if (slowmode == 0) {
                event.reply(Language.getString("SLOWMODE_NOTENABLED", guild)).setEphemeral(true).queue();
                return;
            }

            channel.asTextChannel().getManager().setSlowmode(0).queue();

            event.reply(Language.getString("SLOWMODE_DISABLED", guild).replace("%CHANNELMENTION%", channel.getAsMention())).queue();
        }

    }
}
