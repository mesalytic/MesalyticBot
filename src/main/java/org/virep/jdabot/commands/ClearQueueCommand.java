package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.lavaplayer.TrackScheduler;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;

public class ClearQueueCommand extends SlashCommand {
    public ClearQueueCommand() {
        super("clearqueue", "Clears the current queue.", "music");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler trackScheduler = manager.getScheduler();

        assert guild != null;

        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        if (memberVoiceState.getChannel() == null) {
            event.reply("\u274C - You must be in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("\u274C - I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("\u274C - You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        if (trackScheduler.queue.isEmpty()) {
            event.reply("\u274C - Nothing is in the queue right now.").setEphemeral(true).queue();
            return;
        }

        trackScheduler.queue.clear();
        event.reply("\u2705 - The queue has been cleared.").queue();
    }
}
