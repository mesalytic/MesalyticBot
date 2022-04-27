package org.virep.jdabot.schedulers.jobs;

import net.dv8tion.jda.api.entities.Guild;
import org.virep.jdabot.schedulers.Job;
import org.virep.jdabot.schedulers.tasks.VoiceTimeoutTask;

import java.util.concurrent.TimeUnit;

public class VoiceTimeoutJob extends Job {
    private final VoiceTimeoutTask voiceTimeoutTask;

    public VoiceTimeoutJob(Guild guild) {
        super(5, 0, TimeUnit.MINUTES);
        voiceTimeoutTask = new VoiceTimeoutTask(guild);
    }

    @Override
    public void run() {
    handleTasks(voiceTimeoutTask);
    }
}
