package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.slashcommandhandler.Command;

import java.time.Instant;
import java.util.*;

import static org.virep.jdabot.utils.Utils.getPages;

public class BansCommand implements Command {
    public static Map<Long, Integer> pageNumber = new HashMap<>();

    @Override
    public String getName() {
        return "bans";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "bans")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addSubcommands(
                        new SubcommandData("list", "List banned members.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("\u274C - You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();

        guild.retrieveBanList().queue(bans -> {
            pageNumber.put(event.getChannel().getIdLong(), 0);

            List<List<Guild.Ban>> banPages = getPages(bans, 50);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
                    .setTitle("Ban list for " + event.getGuild().getName())
                    .setFooter("Page " + (pageNumber.get(event.getChannel().getIdLong())  + 1) +"/" + banPages.size())
                    .setTimestamp(Instant.now());

            List<Guild.Ban> page = banPages.get(0);
            StringBuilder embedDescription = new StringBuilder();

            page.forEach(ban -> {
                embedDescription
                        .append("**")
                        .append(ban.getUser().getAsTag())
                        .append("** - ")
                        .append(ban.getReason() != null ? ban.getReason() : "No reason")
                        .append("\n");
            });

            embedBuilder.setDescription(embedDescription.toString());

            event.replyEmbeds(embedBuilder.build())
                    .setActionRow(
                            Button.primary("button:bans:first:" + event.getUser().getId(), "\u23EA"),
                            Button.primary("button:bans:previous:" + event.getUser().getId(), "\u2B05"),
                            Button.primary("button:bans:next:" + event.getUser().getId(), "\u27A1"),
                            Button.primary("button:bans:last:" + event.getUser().getId(), "\u23E9")
                    )
                    .queue();
        });
    }
}
