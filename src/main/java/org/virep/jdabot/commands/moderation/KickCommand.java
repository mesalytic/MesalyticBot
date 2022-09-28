package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class KickCommand implements Command {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Kick someone.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.USER, "member", "the member you want to kick", true),
                        new OptionData(OptionType.STRING, "reason", "The reason of the kick.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("\u274C - You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        String reason = event.getOption("reason") != null ? Objects.requireNonNull(event.getOption("reason")).getAsString() : "No reason.";

        if (member == null) {
            event.reply("\u274C - Somehow this user is not on the server.").setEphemeral(true).queue();
            return;
        }

        member.kick().reason("Kicked by " + event.getUser().getAsTag() + " : " + reason).queue();
        event.reply("\u2705 - Successfully kicked **" + member.getUser().getAsTag() + "** for the reason: **" + reason + "**").queue();
    }
}
