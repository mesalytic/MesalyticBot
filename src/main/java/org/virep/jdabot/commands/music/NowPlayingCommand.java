package org.virep.jdabot.commands.music;

import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.Utils;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.Objects;

public class NowPlayingCommand implements Command {
    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Display informations about the currently playing music.")
                .setGuildOnly(true);
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        LavalinkPlayer player = manager.getPlayer();

        assert guild != null;

        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        if (memberVoiceState.getChannel() == null) {
            event.reply("\u274C - You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel() || player.getPlayingTrack() == null) {
            event.reply("\u274C - I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("\u274C - You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        AudioTrack playingTrack = player.getPlayingTrack();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Currently playing in " + guild.getName(), guild.getIconUrl())
                .setDescription(playingTrack.getInfo().getTitle() + "\n" + Utils.progressBar(manager.getLink().getPlayer().getTrackPosition(), playingTrack.getInfo().getLength()))
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embed).queue();
    }
}
