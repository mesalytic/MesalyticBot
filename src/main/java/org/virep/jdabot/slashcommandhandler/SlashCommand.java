package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class SlashCommand {
    public String name;
    public String description;
    public OptionData options;

    public SlashCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public SlashCommand(String name, String description, OptionData options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OptionData getOptions() {
        return options;
    }
    public boolean hasOptions(OptionData options) {
        return options != null;
    }
}
