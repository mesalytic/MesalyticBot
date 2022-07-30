package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.slashcommandhandler.Command;

public class test implements Command {

    @Override
    public String getName() {
        return "testcommand";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("testcommand", "test!")
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
