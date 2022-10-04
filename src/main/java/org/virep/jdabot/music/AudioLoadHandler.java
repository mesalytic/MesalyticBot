package org.virep.jdabot.music;

import lavalink.client.io.FriendlyException;
import lavalink.client.io.LoadResultHandler;
import lavalink.client.player.track.AudioPlaylist;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.external.Notifier;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.utils.ErrorManager;
import org.virep.jdabot.utils.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

public class AudioLoadHandler {
    private final static Logger log = LoggerFactory.getLogger(AudioLoadHandler.class);
    public static void loadAndPlay(GuildAudioManager manager, String trackURL, SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        manager.openConnection((VoiceChannel) Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
        AudioManagerController.getExistingLink(guild).getRestClient().loadItem(trackURL,  new LoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                log.debug(String.format("track loaded : %s", track.getInfo().getUri()));

                AudioTrackInfo trackInfo = track.getInfo();

                event.getChannel().asTextChannel();

                manager.getScheduler().queue(track, event.getChannel().asTextChannel());

                event.getHook().editOriginal(Language.getString("ALH_TRACKLOADED", guild).replace("%TITLE%", trackInfo.getTitle()).replace("%DURATION%",Utils.formatTrackLength(trackInfo.getLength()))).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.debug(String.format("playlist loaded: %s", playlist.getName()));

                for (AudioTrack track : playlist.getTracks()) {
                    manager.getScheduler().queue(track, event.getChannel().asTextChannel());
                }
                event.getHook().editOriginal(Language.getString("ALH_PLAYLISTLOADED", guild).replace("%NAME%", playlist.getName())).queue();
            }

            @Override
            public void searchResultLoaded(List<AudioTrack> tracks) {
                log.debug(String.format("search result loaded: %s", trackURL));

                AudioTrack track = tracks.get(0);
                manager.getScheduler().queue(track, event.getChannel().asTextChannel());

                event.getHook().editOriginal(Language.getString("ALH_TRACKLOADED", guild).replace("%TITLE%", track.getInfo().getTitle()).replace("%DURATION%",Utils.formatTrackLength(track.getInfo().getLength()))).queue();
            }

            @Override
            public void noMatches() {
                log.debug(String.format("no matches found for: %s", trackURL));

                event.getHook().editOriginal(Language.getString("ALH_NOTFOUND", guild).replace("%SEARCH%", trackURL.replace("ytsearch:", "").replace("scsearch:", "").replace("spsearch:", "").replace("ytmsearch:", ""))).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                ErrorManager.handleNoEvent(exception);

                event.getHook().editOriginal(Language.getString("ALH_ERROR", guild).replace("%MESSAGE%", exception.getMessage())).queue();
            }
        });
    }
}
