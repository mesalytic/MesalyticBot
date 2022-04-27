package org.virep.jdabot.slashcommandhandler;

import kotlin.jvm.internal.Reflection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

            if (cmd.hasOptions(cmd.options) && !cmd.hasSubcommandData(cmd.subcommandData)) { System.out.println(cmd.getName() + 1); update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription()).addOptions(cmd.getOptions())); }
            else if (!cmd.hasOptions(cmd.options) && cmd.hasSubcommandData(cmd.subcommandData)) { System.out.println(cmd.getName() + 2); update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription()).addSubcommands(cmd.getSubcommandData())); }
            else if (!cmd.hasOptions(cmd.options) && !cmd.hasSubcommandData(cmd.subcommandData)) { System.out.println(cmd.getName() + 4); update.addCommands(Commands.slash(cmd.getName(), cmd.getDescription())); }

            slashCommandMap.put(cmd.getName(), cmd);
        });

        update.queue();
    }

    public void addCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Guild guild = jda.getGuildById("418433461817180180");
        assert guild != null;

        CommandListUpdateAction update = guild.updateCommands();

        Reflections reflections = new Reflections("org.virep.jdabot.commands");
        Set<Class<? extends SlashCommand>> commandClasses = reflections.getSubTypesOf(SlashCommand.class);

        for (Class<? extends SlashCommand> commandClass : commandClasses) {
            if (Modifier.isAbstract(commandClass.getModifiers())) {
                continue;
            }

            SlashCommand command = commandClass.getConstructor().newInstance();

            if (command.hasOptions(command.options) && !command.hasSubcommandData(command.subcommandData)) { System.out.println(command.getName() + 1); update.addCommands(Commands.slash(command.getName(), command.getDescription()).addOptions(command.getOptions())); }
            else if (!command.hasOptions(command.options) && command.hasSubcommandData(command.subcommandData)) { System.out.println(command.getName() + 2); update.addCommands(Commands.slash(command.getName(), command.getDescription()).addSubcommands(command.getSubcommandData())); }
            else if (!command.hasOptions(command.options) && !command.hasSubcommandData(command.subcommandData)) { System.out.println(command.getName() + 4); update.addCommands(Commands.slash(command.getName(), command.getDescription())); }


            slashCommandMap.put(command.getName(), command);
        }

        update.queue();
    }

    public Map<String, SlashCommand> getSlashCommandMap() {
        return this.slashCommandMap;
    }
}
