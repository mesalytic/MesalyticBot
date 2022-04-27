package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(Guild guild, LavalinkPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public boolean hasNextTrack() {
        return queue.peek() != null;
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() == null) {
            queue.add(track);
            nextTrack();
            return;
        }
        queue.offer(track);
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();

        if (track == null) {
            if (player.getPlayingTrack() != null) {
                player.stopTrack();
            }

            return;
        }
        player.playTrack(track);
    }
}
