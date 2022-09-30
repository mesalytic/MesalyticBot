package org.virep.jdabot.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.music.TrackScheduler;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class SkipCommand implements Command {
    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Skips the currently playing music.")
                .setGuildOnly(true)
                .addOptions(
                        new OptionData(OptionType.INTEGER, "index", "Index of the music.").setMinValue(1)
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TrackScheduler scheduler = manager.getScheduler();

        assert guild != null;

        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        if (memberVoiceState.getChannel() == null) {
            event.reply("\u274C - You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("\u274C - I'm currently not playing any music!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("\u274C - You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        if (event.getOption("index") != null) {
            scheduler.nextTrack(event.getOption("index", OptionMapping::getAsInt));
        } else {
            scheduler.nextTrack();
        }

        event.reply("\u23E9 - Skipped !").queue();
    }
}