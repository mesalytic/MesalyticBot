package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.handlers.Command;

import java.util.Collections;
import java.util.List;

public class ShutdownCommand implements Command {

    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "shutdown")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOption(OptionType.CHANNEL, "testoption", "test");
    }

    @Override
    public List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        JDA jda = event.getJDA();

        event.reply("Shutdown in progress..").queue();
        jda.shutdown();
    }
}
