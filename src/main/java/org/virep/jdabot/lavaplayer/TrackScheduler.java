package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), true);
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }

    }
    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // Track Start
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }

        // track end
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException excpetion) {
        // Playing track threw exception
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Unable to provide any audio
    }
}
