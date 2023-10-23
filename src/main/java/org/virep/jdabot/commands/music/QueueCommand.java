package org.virep.jdabot.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.music.TrackScheduler;
import org.virep.jdabot.handlers.SlashCommand;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueCommand implements SlashCommand {
    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Returns the current music queue.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Renvoie la file de musique actuelle.")
                .setGuildOnly(true);
    }

    @Override
    public java.util.List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.VOICE_SPEAK, Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        GuildAudioManager guildAudioManager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler trackScheduler = guildAudioManager.getScheduler();

        if (trackScheduler.queue.size() > 0) event.replyEmbeds(paginate(event))
                .addActionRow(Button.secondary("queueFull", Emoji.fromUnicode("\uD83D\uDCC3")))
                .queue();
        else event.reply(Language.getString("QUEUE_NOQUEUE", guild)).setEphemeral(true).queue();
    }

    public static MessageEmbed paginate(GenericInteractionCreateEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager guildAudioManager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler trackScheduler = guildAudioManager.getScheduler();

        StringBuilder queueBuilder = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();

        guildAudioManager.getScheduler().queue.stream().limit(20).forEach(audioTrack -> {
            counter.getAndIncrement();
            queueBuilder
                    .append("\n")
                    .append("[")
                    .append(counter.get())
                    .append("] ")
                    .append(audioTrack.getInfo().getTitle())
                    .append(" - ")
                    .append(audioTrack.getInfo().getAuthor());
        });

        assert guild != null;
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setAuthor(Language.getString("QUEUE_EMBEDAUTHOR", guild).replace("%GUILDNAME%", guild.getName()), guild.getIconUrl())
                .setDescription(queueBuilder.toString())
                .setFooter(Language.getString("QUEUE_EMBEDFOOTER", guild).replace("%TOTALTRACKS%", String.valueOf(trackScheduler.queue.size())));

        return embed.build();
    }
}