package org.virep.jdabot.slashhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class SlashCommand {
    public String name;
    public String description;

    public SlashCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract void execute(SlashCommandInteractionEvent event);


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
