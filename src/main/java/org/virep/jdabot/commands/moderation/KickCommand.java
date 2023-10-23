package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KickCommand implements SlashCommand {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Kick someone.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Exclure un membre.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.USER, "member", "the member you want to kick", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre que vous souhaitez exclure."),
                        new OptionData(OptionType.STRING, "reason", "The reason of the kick.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison de l'exclusion.")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MODERATE_MEMBERS, Permission.KICK_MEMBERS, Permission.VIEW_AUDIT_LOGS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        String reason = event.getOption("reason") != null ? Objects.requireNonNull(event.getOption("reason")).getAsString() : Language.getString("KICK_NOREASON", guild);

        if (member == null) {
            event.reply(Language.getString("KICK_NOTINSERVER", guild)).setEphemeral(true).queue();
            return;
        }

        member.kick().reason(Language.getString("KICK_KICKEDBY", guild).replace("%USERTAG%", event.getUser().getEffectiveName()).replace("%REASON%", reason)).queue();
        event.reply(Language.getString("KICK_KICKED", guild).replace("%USERTAG%", member.getUser().getEffectiveName()).replace("%REASON%", reason)).queue();
    }
}
