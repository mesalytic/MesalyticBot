package org.virep.jdabot;

import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.listeners.AfkListener;
import org.virep.jdabot.listeners.AutoroleListener;
import org.virep.jdabot.listeners.ButtonInteractionListener;
import org.virep.jdabot.listeners.GatewayEventListener;
import org.virep.jdabot.listeners.GuildMessageListener;
import org.virep.jdabot.listeners.InteractionRoleListener;
import org.virep.jdabot.listeners.LogsListener;
import org.virep.jdabot.external.Notifier;
import org.virep.jdabot.listeners.ReactionRoleListener;
import org.virep.jdabot.listeners.SelectMenuInteractionListener;
import org.virep.jdabot.slashcommandhandler.SlashHandler;
import org.virep.jdabot.listeners.SlashListener;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.DatabaseUtils;

import java.net.URI;

public class Main {
    static Main instance;

    Notifier notifier;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        instance = new Main();
        instance.notifier = new Notifier();

        Database.initializeDataSource();

        String devStatus = System.getProperty("dev");

        JDA api = JDABuilder
                .createDefault(Config.get(Boolean.parseBoolean(devStatus) ? "TOKENBETA" : "TOKEN"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES)
                .setActivity(Activity.of(ActivityType.PLAYING, "We Are Back"))
                .addEventListeners(new LogsListener(),
                        new GatewayEventListener(),
                        new InteractionRoleListener(),
                        new GuildMessageListener(),
                        new AutoroleListener(),
                        new ReactionRoleListener(),
                        new AfkListener(),
                        new SelectMenuInteractionListener(),
                        new ButtonInteractionListener())
                .setBulkDeleteSplittingEnabled(false)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableCache(CacheFlag.VOICE_STATE).build().awaitReady();



        SlashHandler slashHandler = new SlashHandler(api);

        api.addEventListener(new SlashListener(slashHandler));

        slashHandler.addCommands();
        log.info("Slash Commands registered");

        instance.notifier.registerTwitterUser(DatabaseUtils.getAllTwitterNames());
        log.info("Twitter Notifiers set up");
    }

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }

        return instance;
    }

    public Notifier getNotifier() {
        return notifier;
    }
}