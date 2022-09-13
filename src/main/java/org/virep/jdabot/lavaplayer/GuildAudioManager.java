package org.virep.jdabot.lavaplayer;

import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.virep.jdabot.Main;

public class GuildAudioManager {
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;

    public GuildAudioManager(Guild guild) {
        this.link = Main.lavalink.getLink(guild);
        this.player = this.link.getPlayer();
        this.scheduler = new TrackScheduler(guild, this.player);

        this.player.addListener(this.scheduler);
    }

    public void openConnection(VoiceChannel voiceChannel) {
        this.link.connect(voiceChannel);
    }

    protected void destroyConnection() {
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
