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
import org.virep.jdabot.handlers.SlashCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VolumeCommand implements SlashCommand {
    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Changes volume for the current queue.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Change le volume de la musique pour la file actuelle.")
                .setGuildOnly(true)
                .addOptions(
                        new OptionData(OptionType.INTEGER, "value", "Volume value", true)
                                .setMinValue(0)
                                .setMaxValue(100)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Volume a modifier (entre 0 et 100)")
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

        OptionMapping valueOption = event.getOption("value");
        assert valueOption != null;

        int value = valueOption.getAsInt();

        if (value < 0 || value > 100) {
            event.reply(Language.getString("VOLUME_OUTOFINDEX", guild)).setEphemeral(true).queue();
        }

        player.setVolume(value);
        event.reply(Language.getString("VOLUME_SET", guild).replace("%VOLUME%", String.valueOf(value))).queue();
    }
}