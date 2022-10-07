package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Collections;
import java.util.List;

public class PingCommand implements Command {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Pong !");
    }

    @Override
    public List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.getJDA().getRestPing().queue(ping -> {
            event.replyFormat("**Pong !**\nGateway: %dms | REST: %dms", event.getJDA().getGatewayPing(), ping.intValue()).queue();
        });
    }
}
