package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SlashCommand {
    public final String name;
    public final String description;
    public final String category;
    public OptionData[] options;
    public SubcommandData[] subcommandData;

    public boolean developerOnly;
    public SlashCommand(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public SlashCommand(String name, String description, String category, SubcommandData[] subcommandData) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.subcommandData = subcommandData;
    }

    public SlashCommand(String name, String description, String category, OptionData[] options) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.options = options;
    }

    public SlashCommand(String name, String description, String category, boolean developerOnly) {
        this.name = name;
        this.description = description;
        this.category = category;
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
