package org.virep.jdabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.virep.jdabot.commands.PingCommand;
import org.virep.jdabot.commands.JoinCommand;
import org.virep.jdabot.commands.TTTCommand;
import org.virep.jdabot.listeners.MessageListener;
import org.virep.jdabot.slashcommandhandler.SlashHandler;
import org.virep.jdabot.listeners.SlashListener;

public class Main {
    public static void main(String[] args) throws Exception {
        JDA api = JDABuilder
                .createDefault(Config.get("TOKEN"))
                .addEventListeners(new MessageListener())
                .enableCache(CacheFlag.VOICE_STATE)
                .build().awaitReady();

        SlashHandler slashHandler = new SlashHandler(api);

        api.addEventListener(new SlashListener(slashHandler));
        slashHandler.addCommands(new PingCommand(),
                new TTTCommand(),
                new JoinCommand());
    }
}