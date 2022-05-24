package org.virep.jdabot.commands;

import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;

import static org.virep.jdabot.Utils.lengthToMillis;

public class SeekCommand extends SlashCommand {
    public SeekCommand() {
        super("seek", "Change the position of the currently playing music.",
                new OptionData[]{
                        new OptionData(OptionType.STRING, "time", "Time to seek to. (format H:M:S or M:S)")
                });
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

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        if (player.isPaused()) {
            event.reply("The music is currently paused !").setEphemeral(true).queue();
            return;
        }

        OptionMapping timeOption = event.getOption("time");

        if (timeOption == null) {
            event.reply("Time not specified").setEphemeral(true).queue();
            return;
        }

        String time = timeOption.getAsString();

        if (lengthToMillis(time) < 0) {
            event.reply("The time specified is inferior or equal to 0.").setEphemeral(true).queue();
            return;
        }

        if (lengthToMillis(time) > player.getPlayingTrack().getInfo().getLength()) {
            event.reply("The time specified is superior to the total duration of the current track !").setEphemeral(true).queue();
            return;
        }

        player.seekTo(lengthToMillis(time));
        event.reply("Successfully seeked at " + time).queue();
    }
}
