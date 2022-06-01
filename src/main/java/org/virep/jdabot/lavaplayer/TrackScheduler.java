package org.virep.jdabot.lavaplayer;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.virep.jdabot.utils.Utils;
import org.virep.jdabot.schedulers.ScheduleHandler;
import org.virep.jdabot.schedulers.jobs.VoiceTimeoutJob;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

public class TrackScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    public final Queue<AudioTrack> queue;
    private final Guild guild;
    private TextChannel channel;

    private boolean looping;
    private ScheduledFuture<?> timeout = null;
    private AudioTrack background = null;

    public TrackScheduler(Guild guild, LavalinkPlayer player) {
        this.guild = guild;
        this.player = player;
        this.channel = null;
        this.looping = false;
        this.queue = new LinkedList<>();
    }

    public boolean hasNextTrack() {
        return queue.peek() != null;
    }

    public void queue(AudioTrack track, TextChannel channel) {
        if (player.getPlayingTrack() == null) {
            this.channel = channel;
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
        channel.sendMessageFormat("\u25B6 - Now playing : **%s** (`%s`)", track.getInfo().getTitle(), Utils.formatTrackLength(track.getInfo().getLength())).queue();
        if (timeout != null) {
            timeout.cancel(true);
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (looping) {
                queue.offer(track);
            }
            nextTrack();
        }
    }

    public boolean isLooping() {
        return this.looping;
    }
    public void setLooping(boolean status) {
        this.looping = status;
    }
}
