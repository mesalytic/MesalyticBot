package org.virep.jdabot.commands.music;

import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class VolumeCommand implements Command {
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

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("\u274C - I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("\u274C - You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        OptionMapping valueOption = event.getOption("value");
        assert valueOption != null;

        int value = valueOption.getAsInt();

        if (value < 0 || value > 100) {
            event.reply("\u274C - The volume value must be between 0 and 100.").setEphemeral(true).queue();
        }

        player.setVolume(value);
        event.reply("\uD83D\uDD0A - Volume has been set to **" + value + "%** !").queue();
    }
}