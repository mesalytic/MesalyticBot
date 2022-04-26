package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.Main;

import javax.annotation.Nullable;
import java.util.HashMap;

public class AudioManagerController {
    private static final HashMap<Guild, GuildAudioManager> managers = new HashMap<>();
    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    public static void registerAudio() {
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.setTrackStuckThreshold(5000L);
    }

    public static GuildAudioManager getGuildAudioManager(Guild guild) {
        GuildAudioManager manager;
        manager = managers.get(guild);

        if (manager == null) {
            synchronized (getGuildAudioManagers()) {
                manager = new GuildAudioManager(guild);
                addGuildAudioManager(guild, manager);
            }
        } else {
            manager = managers.get(guild);
        }
        return manager;
    }

    public static HashMap<Guild, GuildAudioManager> getGuildAudioManagers() {
        return managers;
    }

    private static void addGuildAudioManager(Guild guild, GuildAudioManager manager) {
        managers.put(guild, manager);
    }

    public static void removeGuildAudioManager(Guild guild) {
        managers.remove(guild);
    }

    public static AudioPlayerManager getPlayerManager() {
        System.out.println(playerManager);
        return playerManager;
    }

    public static Link getExistingLink(Guild guild) {
        return Main.lavalink.getExistingLink(guild);
    }
}
