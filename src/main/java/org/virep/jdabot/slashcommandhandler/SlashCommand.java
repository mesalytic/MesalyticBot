package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SlashCommand {
    public final String name;
    public final String description;
    public final boolean isWIP;
    public OptionData[] options;
    public SubcommandData[] subcommandData;

    public SlashCommand(String name, String description, boolean isWIP) {
        this.name = name;
        this.description = description;
        this.isWIP = isWIP;
    }

    public SlashCommand(String name, String description, boolean isWIP, SubcommandData[] subcommandData) {
        this.name = name;
        this.description = description;
        this.isWIP = isWIP;
        this.subcommandData = subcommandData;
    }

    public SlashCommand(String name, String description, boolean isWIP, OptionData[] options) {
        this.name = name;
        this.description = description;
        this.isWIP = isWIP;
        this.options = options;
    }

    public SlashCommand(String name, String description, boolean isWIP, boolean developerOnly) {
        this.name = name;
        this.description = description;
        this.isWIP = isWIP;
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

    public boolean isWIP() {
        return isWIP;
    }
}
