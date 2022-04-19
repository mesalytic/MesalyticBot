package org.virep.jdabot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.slashhandler.SlashCommand;

public class PingCommand extends SlashCommand {
    public PingCommand() {
        super("ping", "pong");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("pong!").queue();
    }
}
