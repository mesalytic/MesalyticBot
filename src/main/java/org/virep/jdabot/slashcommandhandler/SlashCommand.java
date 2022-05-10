package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SlashCommand {
    public final String name;
    public final String description;
    public OptionData[] options;
    public SubcommandData[] subcommandData;

    public boolean developerOnly;
    public SlashCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public SlashCommand(String name, String description, SubcommandData[] subcommandData) {
        this.name = name;
        this.description = description;
        this.subcommandData = subcommandData;
    }

    public SlashCommand(String name, String description, OptionData[] options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }

    public SlashCommand(String name, String description, boolean developerOnly) {
        this.name = name;
        this.description = description;
        this.developerOnly = developerOnly;
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OptionData[] getOptions() {
        return options;
    }

    public SubcommandData[] getSubcommandData() { return subcommandData; }

    public boolean hasSubcommandData(SubcommandData[] subcommandData) { return subcommandData != null; }
    public boolean hasOptions(OptionData[] options) {
        return options != null;
    }

    public boolean isDeveloperOnly() {
        return developerOnly;
    }
}
