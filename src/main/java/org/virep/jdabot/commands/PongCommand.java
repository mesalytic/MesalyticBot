package org.virep.jdabot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

public class PongCommand extends SlashCommand {
    public PongCommand() {
        super("pong", "ping");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("ping!").queue();
    }
}
