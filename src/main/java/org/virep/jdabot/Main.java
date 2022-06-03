package org.virep.jdabot;

import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.virep.jdabot.database.DatabaseConnector;
import org.virep.jdabot.listeners.EventListener;
import org.virep.jdabot.listeners.StarListener;
import org.virep.jdabot.slashcommandhandler.SlashHandler;
import org.virep.jdabot.listeners.SlashListener;
import org.virep.jdabot.utils.Config;

import java.net.URI;
import java.sql.Connection;
public class Main {
    public static JDA PublicJDA = null;
    public static final Connection connectionDB = DatabaseConnector.openConnection();
    public static void main(String[] args) throws Exception {
        JDA api = JDABuilder
                .createDefault(Config.get("TOKEN"))
                .addEventListeners(lavalink, new EventListener(), new StarListener())
                .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor())
                .enableCache(CacheFlag.VOICE_STATE).build().awaitReady();

        SlashHandler slashHandler = new SlashHandler(api);

        api.addEventListener(new SlashListener(slashHandler));

        slashHandler.addCommands();

        lavalink.setAutoReconnect(true);
        lavalink.addNode(URI.create(Config.get("LAVALINKURI")), Config.get("LAVALINKPWD"));

        PublicJDA = api;
    }

    public static final JdaLavalink lavalink = new JdaLavalink(
            "816407992505073725",
            1,
            integer -> PublicJDA
    );
}