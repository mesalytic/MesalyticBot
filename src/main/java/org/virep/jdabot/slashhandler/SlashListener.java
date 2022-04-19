package org.virep.jdabot.slashhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Map;

public class SlashListener extends ListenerAdapter {
    private final SlashHandler slashHandler;

    public SlashListener(SlashHandler slashHandler) {
        this.slashHandler = slashHandler;
    }

    public void onSlashCommand(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Map<String, SlashCommand> commandMap = slashHandler.getSlashCommandMap();

        if (commandMap.containsKey(commandName)) {
            commandMap.get(commandName).execute(event);
        }
    }
}
