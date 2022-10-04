package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.language.Language;
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
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Mettre en sourdine un membre.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                .addSubcommands(
                        new SubcommandData("set", "Timeout a guild member")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Mettre en sourdine un membre.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The member to timeout", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre à mettre en sourdine"),
                                        new OptionData(OptionType.STRING, "duration", "The duration of the timeout.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La durée de la mise en sourdine (maximum 28 jours)"),
                                        new OptionData(OptionType.STRING, "reason", "The reason of the timeout.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison de la mise en sourdine.")
                                ),
                        new SubcommandData("remove", "Remove a timeout from a guild member")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retirer une mise en sourdine à un membre.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The member to remove the timeout.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre à retirer la mise en sourdine"),
                                        new OptionData(OptionType.STRING, "reason", "Reason for the timeout removal.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison du retrait de la mise en sourdine.")
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

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.KICK_MEMBERS)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        assert member != null;

        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_MISSINGPERMISSIONS", guild)).setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_MEMBER),
                        (ex) -> event.reply(Language.getString("ERORRHANDLER_UNKNOWNUSER", guild)).setEphemeral(true).queue());

        OptionMapping reasonMapping = event.getOption("reason");

        if (Objects.equals(event.getSubcommandName(), "set")) {

            OptionMapping durationMapping = event.getOption("duration");
            assert durationMapping != null;

            int duration = timeStringToSeconds(durationMapping.getAsString());

            if (duration == -1) {
                event.reply(Language.getString("MUTE_INVALIDDURATION", guild)).setEphemeral(true).queue();
                return;
            }

            member.timeoutFor(duration, TimeUnit.SECONDS).reason(Language.getString("MUTE_MUTEDBY", guild).replace("%USERTAG%", event.getUser().getAsTag()).replace("%REASON%", (reasonMapping != null ? reasonMapping.getAsString() : Language.getString("MUTE_NOREASON", guild))))
                    .queue(success -> event.reply(Language.getString("MUTE_MUTED", guild).replace("%USERTAG%", member.getUser().getAsTag()).replace("%DURATION%", secondsToSeperatedTime(duration))).queue(), errorHandler);
        } else {

            if (member.isTimedOut()) {
                member.removeTimeout().reason(Language.getString("MUTE_UNMUTEDBY", guild).replace("%USERTAG%", event.getUser().getAsTag()).replace("%REASON%", (reasonMapping != null ? reasonMapping.getAsString() : Language.getString("MUTE_NOREASON", guild))))
                        .queue(success -> event.reply(Language.getString("MUTE_UNMUTED", guild).replace("%USERTAG%", member.getUser().getAsTag())).queue(), errorHandler);
            }
        }
    }
}
