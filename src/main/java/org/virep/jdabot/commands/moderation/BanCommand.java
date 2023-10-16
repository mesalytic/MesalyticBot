package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.Command;
import org.virep.jdabot.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
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
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MODERATE_MEMBERS, Permission.BAN_MEMBERS, Permission.VIEW_AUDIT_LOGS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }
        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_MISSINGPERMISSIONS", guild)).setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_USER),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_UNKNOWNUSER", guild)).setEphemeral(true).queue());

        Member member = event.getOption("user", OptionMapping::getAsMember);
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : Language.getString("BAN_NOREASON", guild).replace("%USERTAG%", event.getUser().getEffectiveName());
        String delTimeString = event.getOption("delete") != null ? event.getOption("delete", OptionMapping::getAsString) : "0s";

        if (member.getId().equals(event.getMember().getId())) {
            event.reply(Language.getString("BAN_YOURSELF", guild)).setEphemeral(true).queue();
            return;
        }

        if (member.isOwner()) {
            event.reply(Language.getString("BAN_OWNER", guild)).setEphemeral(true).queue();
            return;
        }

        if (!event.getMember().canInteract(member)) {
            event.reply(Language.getString("BAN_MODERATOR", guild)).setEphemeral(true).queue();
            return;
        }

        member.ban(Utils.timeStringToSeconds(delTimeString), TimeUnit.SECONDS).reason(reason)
                .queue(success -> event.reply(Language.getString("BAN_BANNED", guild).replace("%USERTAG%", member.getUser().getEffectiveName()).replace("%REASON%", reason)).queue(), errorHandler);
    }
}
