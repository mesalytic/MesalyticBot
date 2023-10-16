package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.handlers.SlashHandler;
import org.virep.jdabot.handlers.TextHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class TextCommandListener extends ListenerAdapter {
    private final TextHandler textHandler;
    private final String prefix = "h!";

    private final static Logger log = LoggerFactory.getLogger(TextCommandListener.class);

    public TextCommandListener(TextHandler textHandler) { this.textHandler = textHandler;}

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getAuthor().getId().equals("604779545018761237")) return;

        String messageContent = event.getMessage().getContentRaw();

        if (messageContent.startsWith("h!")) {
            String[] commandArgs = messageContent.substring(prefix.length()).split("\\s+");
            String commandName = commandArgs[0].toLowerCase();
            String[] args = Arrays.copyOfRange(commandArgs, 1, commandArgs.length);

            textHandler.handleCommand(event, commandName, args);
        }
    }
}
