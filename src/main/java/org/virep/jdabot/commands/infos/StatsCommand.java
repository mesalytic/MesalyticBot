package org.virep.jdabot.commands.infos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.slashcommandhandler.Command;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Objects;

import static org.virep.jdabot.utils.Utils.formatUptime;

public class StatsCommand implements Command {
    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Retrieve stats about the bot.");
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        SelfUser selfUser = event.getJDA().getSelfUser();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(selfUser.getName(), null, selfUser.getAvatarUrl())
                .addField("Creator", Objects.requireNonNull(event.getJDA().getUserById("604779545018761237")).getAsTag(), true)
                .addField("Servers", String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField("Users", String.valueOf(event.getJDA().getUsers().size()), true)
                .addField("Uptime", formatUptime(mxBean.getUptime()), true)
                .addBlankField(true)
                .addField("Made with", "JDA", true)
                .build();

        event.replyEmbeds(embed).queue();
    }
}
