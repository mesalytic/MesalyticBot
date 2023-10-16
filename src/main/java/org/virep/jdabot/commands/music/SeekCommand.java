package org.virep.jdabot.commands.music;

import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.handlers.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.lengthToMillis;

public class SeekCommand implements Command {
    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Change the position of the currently playing music.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Permets d'avancer ou de reculer dans une musique.")
                .setGuildOnly(true)
                .addOptions(
                        new OptionData(OptionType.STRING, "time", "Time to seek to. (format H:M:S or M:S)", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Temps à avancer (format H:M:S ou M:S)")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.VOICE_SPEAK, Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL);

        return permsList;
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
            event.reply(Language.getString("MUSIC_NOVOICECHANNEL", guild)).setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            event.reply(Language.getString("MUSIC_NOMUSIC", guild)).setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply(Language.getString("MUSIC_NOTSAMEVC", guild)).setEphemeral(true).queue();
            return;
        }

        if (player.isPaused()) {
            event.reply(Language.getString("SEEK_PAUSED", guild)).setEphemeral(true).queue();
            return;
        }

        OptionMapping timeOption = event.getOption("time");

        assert timeOption != null;
        String time = timeOption.getAsString();

        if (lengthToMillis(time) < 0) {
            event.reply(Language.getString("SEEK_INFERIOR", guild)).setEphemeral(true).queue();
            return;
        }

        if (lengthToMillis(time) > player.getPlayingTrack().getInfo().getLength()) {
            event.reply(Language.getString("SEEK_SUPERIOR", guild)).setEphemeral(true).queue();
            return;
        }

        player.seekTo(lengthToMillis(time));
        event.reply(Language.getString("SEEK_SEEKED", guild).replace("%TIME%", time)).queue();
    }
}