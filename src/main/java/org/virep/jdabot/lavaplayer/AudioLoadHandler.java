package org.virep.jdabot.lavaplayer;

import lavalink.client.io.FriendlyException;
import lavalink.client.io.LoadResultHandler;
import lavalink.client.player.track.AudioPlaylist;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.Objects;

public class AudioLoadHandler {
    public static void loadAndPlay(GuildAudioManager manager, String trackURL, SlashCommandInteractionEvent event) {
        manager.openConnection((VoiceChannel) Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
        AudioManagerController.getExistingLink(event.getGuild()).getRestClient().loadItem(trackURL,  new LoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo trackInfo = track.getInfo();
                manager.getScheduler().queue(track, event.getTextChannel());

                event.replyFormat("Adding to Queue: %s by %s", trackInfo.getTitle(), trackInfo.getAuthor()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    manager.getScheduler().queue(track, event.getTextChannel());
                }
                event.replyFormat("Adding Playlist to Queue: %s", playlist.getName()).queue();
            }

            @Override
            public void searchResultLoaded(List<AudioTrack> tracks) {
                AudioTrack track = tracks.get(0);
                manager.getScheduler().queue(track, event.getTextChannel());

                event.replyFormat("Adding to Queue: %s by %s", track.getInfo().getTitle(), track.getInfo().getAuthor()).queue();
            }

            @Override
            public void noMatches() {
                System.out.println("aaaaaaaaa");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println(exception.getMessage());
            }
        });
    }
}
