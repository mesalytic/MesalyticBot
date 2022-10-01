package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.Utils;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class BanCommand implements Command {
    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Ban a user.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Bannir un membre.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The User to ban.", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre a bannir."),
                        new OptionData(OptionType.STRING, "reason", "The reason of the ban.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison du bannissement."),
                        new OptionData(OptionType.STRING, "delete", "Amount of time delete messages. (between 0 seconds - 7 days)")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le temps de suppression des messages. (entre 0 secondes - 7 jours)")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("\u274C - You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }
        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply("\u274C - The specified member cannot be banned, because of permission discrepancy.").setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_USER),
                        (ex) -> event.reply("\u274C - The specified member cannot be banned, as they are no longer in the server.").setEphemeral(true).queue());

        Member member = event.getOption("user", OptionMapping::getAsMember);
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Banned by " + event.getUser().getAsTag() + ": No reason provided";
        String delTimeString = event.getOption("delete") != null ? event.getOption("delete", OptionMapping::getAsString) : "0s";

        if (member.getId().equals(event.getMember().getId())) {
            event.reply("\u274C - You cannot ban yourself.").setEphemeral(true).queue();
            return;
        }
        
        if (member.isOwner()) {
            event.reply("\u274C - The specified member cannot be banned, because they are the server owner.").setEphemeral(true).queue();
            return;
        }

        if (!event.getMember().canInteract(member)) {
            event.reply("\u274C - The specified member cannot be banned, because of the member has a higher permission position.").setEphemeral(true).queue();
            return;
        }

        member.ban(Utils.timeStringToSeconds(delTimeString), TimeUnit.SECONDS).reason(reason)
                .queue(success -> event.reply("The member **" + member.getUser().getAsTag() + "** has been banned. (Reason: **" + reason + "**)").queue(), errorHandler);
    }
}
