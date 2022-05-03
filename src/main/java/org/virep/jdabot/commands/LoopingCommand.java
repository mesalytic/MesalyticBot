package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.lavaplayer.TrackScheduler;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;

public class LoopingCommand extends SlashCommand {
    public LoopingCommand() {
        super("looping", "Loops the currently playing music.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;

        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler trackScheduler = manager.getScheduler();

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

        boolean looping = trackScheduler.isLooping();

        if (looping) event.reply("The current music is not looped anymore.").queue();
        else event.reply("The current music is now looped.").queue();

        trackScheduler.setLooping(!trackScheduler.isLooping());
    }
}
