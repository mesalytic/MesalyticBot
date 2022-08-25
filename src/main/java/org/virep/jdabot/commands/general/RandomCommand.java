package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class RandomCommand implements Command {
    @Override
    public String getName() {
        return "random";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Returns random values for a specific theme.")
                .addSubcommands(
                        new SubcommandData("coinflip", "Heads or tails ?"),
                        new SubcommandData("dice", "Returns a random number.")
                                .addOption(OptionType.INTEGER, "maxvalue", "The maximum possible number it will return.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();

        assert subcommandName != null;
        if (subcommandName.equals("coinflip")) {
            String[] sides = {"heads", "tails"};

            event.replyFormat("It landed on...** %s **!", sides[(int) Math.floor(Math.random() * sides.length)]).queue();
        }

        if (subcommandName.equals("dice")) {
            int maxValue = event.getOption("maxvalue") != null ? Objects.requireNonNull(event.getOption("maxvalue")).getAsInt() : 6;

            event.replyFormat("The dice returned...** %d **!", (int)randomInt(maxValue)).queue();
        }
    }

    private double randomInt(int max) {
        return Math.floor(Math.random() * (Math.floor(max) - Math.ceil(1) + 1)) + Math.ceil(1);
    }
}
