package org.virep.jdabot.music;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.event.PlayerPauseEvent;
import lavalink.client.player.event.PlayerResumeEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStartEvent;
import lavalink.client.player.event.TrackStuckEvent;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.virep.jdabot.utils.ErrorManager;
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
    private final AudioTrack background = null;

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
                /*if (background != null) {
                    player.playTrack(background);
                }*/
            timeout = ScheduleHandler.registerUniqueJob(new VoiceTimeoutJob(guild));

            if (player.getPlayingTrack() != null) {
                player.stopTrack();
            }

            return;
        }
        player.playTrack(track);

    }

    public void nextTrack(int count) {
        AudioTrack track = null;

        for (int i = 0; i < count; i++) {
            track = queue.poll();
        }

        if (track == null) {
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



    @Override
    public void onEvent(PlayerEvent event) {
        if (event instanceof TrackExceptionEvent) {
            Throwable exception = ((TrackExceptionEvent) event).getException();

            ErrorManager.handleNoEvent(exception);
            channel.sendMessage("An error has occured while playing the music : `" + exception.getMessage() + "`").queue();
        } else if (event instanceof TrackStuckEvent) {

            channel.sendMessage("The music got somehow stuck.").queue();
            System.out.println(((TrackStuckEvent) event).getTrack().getInfo().getTitle());
        }
    }

    public boolean isLooping() {
        return this.looping;
    }
    public void setLooping(boolean status) {
        this.looping = status;
    }
}