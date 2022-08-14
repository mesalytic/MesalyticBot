package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class KickCommand implements Command {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Kick someone.")
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
        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        String reason = event.getOption("reason") != null ? Objects.requireNonNull(event.getOption("reason")).getAsString() : "No reason.";

        if (member == null) {
            event.reply("Somehow this user is not on the server.").setEphemeral(true).queue();
            return;
        }

        member.kick("Kicked by " + event.getUser().getAsTag() + " : " + reason).queue();
        event.reply("Successfully kicked **" + member.getUser().getAsTag() + "** for the reason: **" + reason + "**").queue();
    }
}
