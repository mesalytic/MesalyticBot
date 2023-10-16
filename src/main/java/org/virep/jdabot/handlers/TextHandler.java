package org.virep.jdabot.handlers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextHandler {
    public static final Map<String, TextCommand> textCommandMap = new HashMap<>();


    // IntelliJ thinks CommandListUpdateAction#addCommands() result is ignored
    // though it is queued line 38

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void addCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, FileNotFoundException {
        Reflections reflectionsCommands = new Reflections("org.virep.jdabot.textcommands");
        Set<Class<? extends TextCommand>> commandClasses = reflectionsCommands.getSubTypesOf(TextCommand.class);

        for (Class<? extends TextCommand> commandClass : commandClasses) {
            if (Modifier.isAbstract(commandClass.getModifiers())) {
                continue;
            }

            TextCommand command = commandClass.getConstructor().newInstance();

            textCommandMap.put(command.getName(), command);
        }
    }

    public Map<String, TextCommand> getTextCommandMap() {
        return textCommandMap;
    }

    public void handleCommand(MessageReceivedEvent event, String commandName, String[] args) {
        TextCommand command = textCommandMap.get(commandName);

        if (command != null) {
            if (command.isDev() && !event.getChannel().getId().equals("580657468821078017")) return;

            command.execute(event, args);
        }
    }
}
