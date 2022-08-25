package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.FileNotFoundException;

public interface Command {
    public String getName();
    public SlashCommandData getCommandData();

    public boolean isDev();

    public void execute(SlashCommandInteractionEvent event);
}
