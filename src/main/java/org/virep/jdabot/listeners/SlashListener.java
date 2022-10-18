package org.virep.jdabot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.slashcommandhandler.SlashHandler;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SlashListener extends ListenerAdapter {
    private final SlashHandler slashHandler;

    private final static Logger log = LoggerFactory.getLogger(SlashListener.class);

    public SlashListener(SlashHandler slashHandler) {
        this.slashHandler = slashHandler;
    }

    private final WebhookClient webhook = WebhookClient.withUrl(Config.get("DISCORD_CMD_WEBHOOKURL"));

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Commands do not work in DMs.").queue();
            return;
        }
        String commandName = event.getName();
        Map<String, Command> commandMap = slashHandler.getSlashCommandMap();

        if (commandMap.containsKey(commandName)) {

            Command command = commandMap.get(commandName);
            List<Permission> missingBotPermissions = Utils.getMissingPermissions(event.getGuild().getSelfMember().getPermissions(), command.getBotPermissions());

            if (!missingBotPermissions.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                missingBotPermissions.forEach(missingPerm -> {
                    sb.append("`%PERMISSION%`".replace("%PERMISSION%", missingPerm.getName()));
                });

                // TODO: Add translation to MissingPerm String
                event.reply("X - The bot doesn't have these permissions : %PERMISSIONS%".replace("%PERMISSIONS%", sb.toString())).queue();
            }

            webhook.send("``` " + event.getUser().getAsTag() + " (" + event.getUser().getId() + ") - " + event.getInteraction().getCommandString() + "```");
            log.debug(String.format("command executed by %s : %s", event.getUser().getAsTag(), event.getInteraction().getCommandString()));
            commandMap.get(commandName).execute(event);
        }
    }
}
