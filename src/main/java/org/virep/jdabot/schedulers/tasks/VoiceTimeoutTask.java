package org.virep.jdabot.schedulers.tasks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import org.virep.jdabot.commands.music.StopCommand;
import org.virep.jdabot.schedulers.Task;

public class VoiceTimeoutTask implements Task {
    private final Guild guild;
    private final GuildVoiceState voiceState;

    public VoiceTimeoutTask(Guild guild) {
        this.guild = guild;
        this.voiceState = guild.getSelfMember().getVoiceState();
    }
    @Override
    public void run() {
        if (voiceState != null && voiceState.inAudioChannel()) {
            new StopCommand().disconnect(guild);
        }
    }
}