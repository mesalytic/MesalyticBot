package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.virep.jdabot.slashcommandhandler.SlashCommand;
import org.virep.jdabot.slashcommandhandler.SlashHandler;

import javax.annotation.Nonnull;
import java.util.Map;

public class SlashListener extends ListenerAdapter {
    private final SlashHandler slashHandler;

    public SlashListener(SlashHandler slashHandler) {
        this.slashHandler = slashHandler;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Map<String, SlashCommand> commandMap = slashHandler.getSlashCommandMap();

        if (commandMap.containsKey(commandName)) {
            commandMap.get(commandName).execute(event);
        }
    }
}
