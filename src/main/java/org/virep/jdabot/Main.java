package org.virep.jdabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.virep.jdabot.commands.PingCommand;
import org.virep.jdabot.commands.PongCommand;
import org.virep.jdabot.slashhandler.SlashHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        JDA api = JDABuilder
                .createDefault(Config.get("TOKEN"))
                .addEventListeners(new MessageListener())
                .build().awaitReady();

        SlashHandler slashHandler = new SlashHandler(api);

        slashHandler.addCommands(new PingCommand());
        slashHandler.listen();
    }
}