package org.virep.jdabot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    public void openConnection(VoiceChannel voiceChannel, TextChannel textChannel) {
        try {
            this.link.connect(voiceChannel);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void loadAndPlay(GuildAudioManager gam, String trackURL, SlashCommandInteractionEvent event) {
        AudioManagerController.getPlayerManager().loadItemOrdered(gam, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                getScheduler().queue(track);

                event.replyFormat("Adding to Queue: %s by %s", track.getInfo().title, track.getInfo().author).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    getScheduler().queue(track);
                }
                event.replyFormat("Adding Playlist to Queue: %s", playlist.getName()).queue();
            }

            @Override
            public void noMatches() {
                System.out.println("aaaaaaaaa");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                throw exception;
            }
        });
    }

    public void closeConnection() {
        this.link.destroy();
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
