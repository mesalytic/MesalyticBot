package org.virep.jdabot.commands;

import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.SlashCommand;
import org.virep.jdabot.Utils;

import java.time.Instant;
import java.util.Objects;

public class NowPlayingCommand extends SlashCommand {
    public NowPlayingCommand() {
        super("nowplaying", "Display informations the currently playing music.");
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
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel() || player.getPlayingTrack() == null) {
            event.reply("I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        AudioTrack playingTrack = player.getPlayingTrack();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Currently playing in " + guild.getName())
                .setDescription(playingTrack.getInfo().getTitle() + "\n" + Utils.progressBar(manager.getLink().getPlayer().getTrackPosition(), playingTrack.getInfo().getLength()))
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embed).queue();
    }
}
