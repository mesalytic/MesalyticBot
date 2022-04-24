package org.virep.jdabot.slashcommandhandler;

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

    // IntelliJ thinks CommandListUpdateAction#addCommands() result is ignored
    // though it is queued line 38

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void addCommands(@Nonnull SlashCommand... commands) {
        Guild guild = jda.getGuildById("418433461817180180");
        assert guild != null;

        CommandListUpdateAction update = guild.updateCommands();

        Arrays.stream(commands).forEach(cmd -> {
            if (cmd.hasOptions(cmd.options)) update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription()).addOptions(cmd.options));
            else update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription()));

            slashCommandMap.put(cmd.getName(), cmd);
        });

        update.queue();
    }

    public Map<String, SlashCommand> getSlashCommandMap() {
        return this.slashCommandMap;
    }
}
