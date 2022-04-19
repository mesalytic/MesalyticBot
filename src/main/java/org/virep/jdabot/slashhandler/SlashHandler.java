package org.virep.jdabot.slashhandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SlashHandler {
    private final JDA jda;
    private final SlashListener listener;
    private final Map<String, SlashCommand> slashCommandMap = new HashMap<>();

    public SlashHandler(JDA jda) {
        this.jda = jda;
        listener = new SlashListener(this);
    }

    public void addCommand(@Nonnull SlashCommand command) {
        Guild guild = jda.getGuildById("418433461817180180");
        assert guild != null;
        guild.updateCommands().addCommands(Commands.slash(command.getName(), command.getDescription())).queue();
    }

    public synchronized void listen() {
        jda.addEventListener(this.listener);
    }

    public void addCommands(@Nonnull SlashCommand... commands) {
        Arrays.stream(commands).forEach(this::addCommand);
    }

    public Map<String, SlashCommand> getSlashCommandMap() {
        return this.slashCommandMap;
    }
}
