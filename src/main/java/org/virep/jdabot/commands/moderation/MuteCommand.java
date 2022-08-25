package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.virep.jdabot.slashcommandhandler.Command;

public class MuteCommand implements Command {
    @Override
    public String getName() {
        return "timeout";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Timeout or un-timeout someone")
                .addSubcommands(
                        new SubcommandData("set", "Timeout a guild member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The member to timeout", true),
                                        new OptionData(OptionType.STRING, "duration", "The duration of the timeout.", true),
                                        new OptionData(OptionType.STRING, "reason", "The reason of the timeout.")
                                ),
                        new SubcommandData("remove", "Remove a timeout from a guild member")
                                .addOption(OptionType.USER, "member", "The member to remove the timeout.", true)
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }
}
