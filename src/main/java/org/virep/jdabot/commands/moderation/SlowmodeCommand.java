package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
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
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Configure the slowmode for a specific channel.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addSubcommands(
                        new SubcommandData("set", "Configure the slowmode duration.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "duration", "The slowmode duration. Default is 0 seconds.", true),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel to configure. Default is interaction channel.", true)
                                ),
                        new SubcommandData("off", "Disables the slowmode for that specific channel.")
                                .addOption(OptionType.CHANNEL, "channel", "The channel that has the slowmode.", true)
                );

    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping channelMapping = event.getOption("channel");
        assert channelMapping != null;

        GuildChannelUnion channel = channelMapping.getAsChannel();

        if (channel.getType() != ChannelType.TEXT) {
            event.reply("The channel you selected is not a text channel.").setEphemeral(true).queue();
            return;
        }

        if (Objects.equals(event.getSubcommandName(), "set")) {
            OptionMapping durationMapping = event.getOption("duration");
            assert durationMapping != null;

            try {
                int slowmodeSecs = (int) Duration.parse("PT" + durationMapping.getAsString().toUpperCase().replace(" ", "")).getSeconds();

                if (slowmodeSecs > TextChannel.MAX_SLOWMODE || slowmodeSecs < 0) {
                    event.reply("You must specify a duration between **0 seconds** and **6 hours**.").setEphemeral(true).queue();
                    return;
                }

                channel.asTextChannel().getManager().setSlowmode(slowmodeSecs).queue();

                event.reply("The slowmode for " + channel.getAsMention() + " has been successfully set to **" + secondsToSeperatedTime(slowmodeSecs) + "** !").queue();
            } catch (DateTimeParseException e) {
                event.reply("The duration you specified is not valid. Please specify a duration between **0 seconds** and **6 hours**.").setEphemeral(true).queue();
            }

        } else {
            int slowmode = channel.asTextChannel().getSlowmode();

            if (slowmode == 0) {
                event.reply("The slowmode for this channel is currently not enabled.").setEphemeral(true).queue();
                return;
            }

            channel.asTextChannel().getManager().setSlowmode(0).queue();

            event.reply("The slowmode for " + channel.getAsMention() + " has been successfully **disabled** !").queue();
        }

    }
}
