package org.virep.jdabot.slashhandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SlashHandler {
    private final JDA jda;
    private final Map<String, SlashCommand> slashCommandMap = new HashMap<>();

    public SlashHandler(JDA jda) {
        this.jda = jda;
    }

    public void addCommands(@Nonnull SlashCommand... commands) {
        Guild guild = jda.getGuildById("418433461817180180");
        assert guild != null;

        CommandListUpdateAction update = guild.updateCommands();

        Arrays.stream(commands).forEach(cmd -> {
            // result is not forwarded in specific line, which triggers a warning
            // even though it IS forwarded later outside the for each loop, so its ignored
            //noinspection ResultOfMethodCallIgnored
            update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription()));
            slashCommandMap.put(cmd.getName(), cmd);
        });

        update.queue();
    }

    public Map<String, SlashCommand> getSlashCommandMap() {
        return this.slashCommandMap;
    }
}
