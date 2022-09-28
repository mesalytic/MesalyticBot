package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.slashcommandhandler.Command;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.virep.jdabot.utils.Utils.secondsToSeperatedTime;
import static org.virep.jdabot.utils.Utils.timeStringToSeconds;

public class MuteCommand implements Command {
    @Override
    public String getName() {
        return "timeout";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Timeout or un-timeout someone")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                .addSubcommands(
                        new SubcommandData("set", "Timeout a guild member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The member to timeout", true),
                                        new OptionData(OptionType.STRING, "duration", "The duration of the timeout.", true),
                                        new OptionData(OptionType.STRING, "reason", "The reason of the timeout.")
                                ),
                        new SubcommandData("remove", "Remove a timeout from a guild member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The member to remove the timeout.", true),
                                        new OptionData(OptionType.STRING, "reason", "Reason for the timeout removal.")
                                )
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("\u274C - You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        assert member != null;

        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply("\u274C - The specified member cannot be timed out, because of permission discrepancy.").setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_MEMBER),
                        (ex) -> event.reply("\u274C - The specified member cannot be timed out, as they are no longer in the server.").setEphemeral(true).queue());

        OptionMapping reasonMapping = event.getOption("reason");

        if (Objects.equals(event.getSubcommandName(), "set")) {

            OptionMapping durationMapping = event.getOption("duration");
            assert durationMapping != null;

            int duration = timeStringToSeconds(durationMapping.getAsString());

            if (duration == -1) {
                event.reply("\u274C - The duration you specified is invalid. Example: `1d 3h 30m 13s`").setEphemeral(true).queue();
                return;
            }

            member.timeoutFor(duration, TimeUnit.SECONDS).reason("Muted by " + event.getUser().getAsTag() + ": " + (reasonMapping != null ? reasonMapping.getAsString() : "No reason specified."))
                    .queue(success -> event.reply("\u2705 - Member **" + member.getUser().getAsTag() + "** has been timed out for **" + secondsToSeperatedTime(duration) + "** !").queue(), errorHandler);
        } else {

            if (member.isTimedOut()) {
                member.removeTimeout().reason("Unmuted by " + event.getUser().getAsTag() + ": " + (reasonMapping != null ? reasonMapping.getAsString() : "No reason specified."))
                        .queue(success -> event.reply("\u2705 - Member **" + member.getUser().getAsTag() + "** has been un-timed out. !").queue(), errorHandler);
            }
        }
    }
}
