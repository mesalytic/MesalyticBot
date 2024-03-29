package org.virep.jdabot.music;

import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.Main;

public class GuildAudioManager {
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;

    private static final Logger log = LoggerFactory.getLogger(GuildAudioManager.class);

    public GuildAudioManager(Guild guild) {
        this.link = Main.lavalink.getLink(guild);
        this.player = this.link.getPlayer();
        this.scheduler = new TrackScheduler(guild, this.player);

        this.player.addListener(this.scheduler);
    }

    public void openConnection(AudioChannel channel) {
        log.debug(String.format("connection open for vc: %s", channel.getId()));
        this.link.connect(channel);
    }

    protected void destroyConnection() {
        log.debug("connection destroyed");

        scheduler.queue.clear();
        player.stopTrack();
        player.removeListener(scheduler);
        link.resetPlayer();
        link.destroy();
    }

    public void resetPlayer(Guild guild) {
        link.resetPlayer();
    }

    public JdaLink getLink() {
        return link;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }
}