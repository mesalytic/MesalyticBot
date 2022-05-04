package org.virep.jdabot.commands;

import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.lavaplayer.TrackScheduler;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;

//TODO: Add support for skipping to a specific music (index queue)
// Due to Discord limitations, it needs to be in a seperate file (options w or w/o)
public class SkipCommand extends SlashCommand {
    public SkipCommand() {
        super("skip", "Skips the currently playing music.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler scheduler = manager.getScheduler();

        assert guild != null;

        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        if (memberVoiceState.getChannel() == null) {
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        scheduler.nextTrack();

        event.reply("Skipped!").queue();
    }
}
