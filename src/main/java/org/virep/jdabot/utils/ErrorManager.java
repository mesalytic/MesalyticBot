package org.virep.jdabot.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorManager {

    private final static Logger log = LoggerFactory.getLogger(ErrorManager.class);
    static WebhookClient client = WebhookClient.withUrl(Config.get("DISCORD_ERROR_WEBHOOKURL"));

    public static void handleNoEvent(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);

        send(sw, null);

        log.error(sw.toString());

    }

    public static void handle(Throwable throwable, SlashCommandInteractionEvent event) {
        // we should assume the event reply is being deferred when the error is thrown

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);

        send(sw, event);

        log.error(sw.toString());
        event.getChannel().sendMessage("An error has occurred: `" + throwable.getMessage() + "`\n\nThe error has been sent to the dev team.").queue();
    }

    private static void send(StringWriter sw, @Nullable SlashCommandInteractionEvent event) {
        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        StringBuilder sb = new StringBuilder();
        sb.append("============================\n**Server**: ")
                .append(event == null ? "N/A" : event.getGuild().getName())
                .append(" (`")
                .append(event == null ? "N/A" : event.getGuild().getId())
                .append("`)\n**Command**: ")
                .append(event == null ? "N/A" : event.getCommandString())
                .append(" (`")
                .append(event == null ? "N/A" : event.getCommandId())
                .append("`)");


        wmb.setContent(sb.toString());

        wmb.setUsername("Errors V3");
        wmb.addFile("stacktrace.txt", sw.toString().getBytes());

        client.send(wmb.build());
    }
}
