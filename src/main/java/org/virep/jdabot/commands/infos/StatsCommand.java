package org.virep.jdabot.commands.infos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.formatUptime;

public class StatsCommand implements Command {
    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Retrieve stats about the bot.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche les statistiques à propos du bot");
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MESSAGE_EMBED_LINKS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        SelfUser selfUser = event.getJDA().getSelfUser();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(selfUser.getName(), null, selfUser.getAvatarUrl())
                .addField(Language.getString("STATS_CREATOR", guild), Objects.requireNonNull(event.getJDA().getUserById("604779545018761237")).getAsTag(), true)
                .addField(Language.getString("STATS_SERVERS", guild), String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField(Language.getString("STATS_USERS", guild), String.valueOf(event.getJDA().getUsers().size()), true)
                .addField(Language.getString("STATS_UPTIME", guild), formatUptime(mxBean.getUptime()), true)
                .addBlankField(true)
                .addField(Language.getString("STATS_MADEWITH", guild), "Java (JDA 5.0.0 alpha 20)", true)
                .build();

        event.replyEmbeds(embed).queue();
    }
}
