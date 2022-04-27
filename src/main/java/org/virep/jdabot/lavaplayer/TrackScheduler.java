package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.schedulers.ScheduleHandler;
import org.virep.jdabot.schedulers.jobs.VoiceTimeoutJob;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

public class TrackScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    final BlockingQueue<AudioTrack> queue;
    private ScheduledFuture<?> timeout = null;
    private AudioTrack background = null;
    private final Guild guild;

    public TrackScheduler(Guild guild, LavalinkPlayer player) {
        this.guild = guild;
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
                if (background != null) {
                    background = background.makeClone();
                    player.playTrack(background);
                }
                timeout = ScheduleHandler.registerUniqueJob(new VoiceTimeoutJob(guild));

                if (player.getPlayingTrack() != null) {
                    player.stopTrack();
                }

                return;
            }
            player.playTrack(track);

    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        if (timeout != null) {
            timeout.cancel(true);
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}