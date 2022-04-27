package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.Main;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;

public class AudioManagerController {
    private static final HashMap<Guild, GuildAudioManager> managers = new HashMap<>();
    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    public static void registerAudio() {
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.setTrackStuckThreshold(5000L);
    }

    public static GuildAudioManager getGuildAudioManager(Guild guild) {
        GuildAudioManager manager = managers.get(guild);

        if (manager == null) {
            synchronized (getGuildAudioManagers()) {
                manager = new GuildAudioManager(guild);
                addGuildAudioManager(guild, manager);
            }
        }
        return manager;
    }

    public static HashMap<Guild, GuildAudioManager> getGuildAudioManagers() {
        return managers;
    }

    private static void addGuildAudioManager(Guild guild, GuildAudioManager manager) {
        managers.put(guild, manager);
    }

    public static void resetStuckGuildAudioManager(Guild guild, Queue<AudioTrack> queue) {
        destroyGuildAudioManager(guild);
        getGuildAudioManager(guild).getScheduler().queue.addAll(queue);
    }

    public static void destroyGuildAudioManager(Guild guild) {
        if (managers.containsKey(guild)) {
            managers.get(guild).destroyConnection();
            managers.remove(guild);
        }
    }

    public static AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public static boolean hasLink(Guild guild) {
        return Main.lavalink.getExistingLink(guild) != null;
    }
    public static Link getExistingLink(Guild guild) {
        return Main.lavalink.getExistingLink(guild);
    }

    public static boolean isLinkConnected(Guild guild) {
        return Objects.requireNonNull(Main.lavalink.getExistingLink(guild)).getState() == Link.State.CONNECTED;
    }
}