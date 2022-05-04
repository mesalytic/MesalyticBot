package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;

public class JoinCommand extends SlashCommand {
    public JoinCommand() {
        super("join",
                "Join channel to play music"
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        GuildVoiceState voiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert voiceState != null;
        if (voiceState.getChannel() == null) {
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }
        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();

        if (selfVoiceState != null && selfVoiceState.inAudioChannel()) {
            event.reply("I'm already in a voice channel!").setEphemeral(true).queue();
            return;
        }
        VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceState.getChannel().getIdLong());

        assert voiceChannel != null;
        manager.openConnection(voiceChannel);

        event.replyFormat("\uD83D\uDD0A I joined `%s`", voiceChannel.getName()).queue();
    }
}
