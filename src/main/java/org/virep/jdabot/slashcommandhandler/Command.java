package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.FileNotFoundException;

public interface Command {
    public String getName();
    public CommandData getCommandData();

    public boolean isDev();

    public void execute(SlashCommandInteractionEvent event);
}
