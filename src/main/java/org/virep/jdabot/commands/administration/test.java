package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.slashcommandhandler.Command;

public class test implements Command {

    @Override
    public String getName() {
        return "testcommand";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("testcommand", "test!")
                .addOption(OptionType.CHANNEL, "testoption", "test");
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("test command class perfect!").queue();
    }
}
