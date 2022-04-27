package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

public class StopCommand extends SlashCommand {
    public StopCommand() {
        super("stop", "Stops the currently played music.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        AudioManagerController.destroyGuildAudioManager(event.getGuild());

        event.reply("Disconnected !").queue();
    }

    public void disconnect(Guild guild) {
        AudioManagerController.destroyGuildAudioManager(guild);
    }
}
