package org.virep.jdabot.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

        log.error(sw.toString());
    }

    public static void handle(Throwable throwable, SlashCommandInteractionEvent event) {
        // we should assume the event reply is being deferred when the error is thrown

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setContent(
                "============================\n**Server**: " + event.getGuild().getName() + " (`" + event.getGuild().getId() + "`)\n**Command**: " + event.getCommandString() + " (`" + event.getCommandId() + "`)");

        wmb.setUsername("Errors V3");
        wmb.addFile("stacktrace.txt", sw.toString().getBytes());

        client.send(wmb.build());

        log.error(sw.toString());
        event.getChannel().sendMessage("An error has occurred: `" + throwable.getMessage() + "`\n\nThe error has been sent to the dev team.").queue();
    }
}
