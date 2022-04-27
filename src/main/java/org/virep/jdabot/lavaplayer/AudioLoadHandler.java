package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class AudioLoadHandler {
    public void loadAndPlay(GuildAudioManager manager, String trackURL, SlashCommandInteractionEvent event) {
        manager.openConnection((VoiceChannel) Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
        AudioManagerController.getPlayerManager().loadItemOrdered(manager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                manager.getScheduler().queue(track);

                event.replyFormat("Adding to Queue: %s by %s", track.getInfo().title, track.getInfo().author).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    manager.getScheduler().queue(track);
                }
                event.replyFormat("Adding Playlist to Queue: %s", playlist.getName()).queue();
            }

            @Override
            public void noMatches() {
                System.out.println("aaaaaaaaa");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                throw exception;
            }
        });
    }
}
