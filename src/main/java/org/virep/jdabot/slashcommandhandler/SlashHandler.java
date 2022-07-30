package org.virep.jdabot.slashcommandhandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO: For production stage, add developerOnly commands through Guild#updateCommands()
public class SlashHandler {
    private final JDA jda;
    public static final Map<String, Command> slashCommandMap = new HashMap<>();

    public SlashHandler(JDA jda) {
        this.jda = jda;
    }

    // IntelliJ thinks CommandListUpdateAction#addCommands() result is ignored
    // though it is queued line 38

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void addCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, FileNotFoundException {
        Guild guild = jda.getGuildById("418433461817180180");
        assert guild != null;

        CommandListUpdateAction globalUpdate = jda.updateCommands();
        CommandListUpdateAction guildUpdate = guild.updateCommands();

        Reflections reflectionsCommands = new Reflections("org.virep.jdabot.commands");
        Set<Class<? extends Command>> commandClasses = reflectionsCommands.getSubTypesOf(Command.class);

        for (Class<? extends Command> commandClass : commandClasses) {
            if (Modifier.isAbstract(commandClass.getModifiers())) {
                continue;
            }

            Command command = commandClass.getConstructor().newInstance();

            if (command.isDev()) guildUpdate.addCommands(command.getCommandData());
            else globalUpdate.addCommands(command.getCommandData());

            slashCommandMap.put(command.getName(), command);
        }

        globalUpdate.queue();
        guildUpdate.queue();
    }

    public Map<String, Command> getSlashCommandMap() {
        return slashCommandMap;
    }
}
