package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.virep.jdabot.lavaplayer.AudioLoadHandler;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class PlayCommand extends SlashCommand {

    public PlayCommand() {
        super("play",
                "Play music on your voice channel!",
                new SubcommandData[] {
                        new SubcommandData("youtube", "Play YouTube urls!").addOption(OptionType.STRING, "url", "YouTube Video or Playlist URL").addOption(OptionType.STRING, "search", "Search string"),
                        new SubcommandData("soundcloud", "Play SoundCloud urls!").addOption(OptionType.STRING, "url", "SoundCloud URL"),
                        new SubcommandData("spotify", "Play Spotify URLs").addOption(OptionType.STRING, "url", "url").addOption(OptionType.STRING, "search", "search")
                }
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(event.getGuild());

        Guild guild = event.getGuild();

        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        OptionMapping urlOption = event.getOption("url");
        OptionMapping searchOption = event.getOption("search");

        String result = "";

        if (urlOption == null && searchOption == null) {
            event.reply("You must specify a URL or a search query.").setEphemeral(true).queue();
            return;
        } else {
            if (urlOption != null) {
                try {
                    new URL(urlOption.getAsString());
                    result = urlOption.getAsString();
                } catch (MalformedURLException e) {
                    event.reply("You must specify a valid URL.").setEphemeral(true).queue();
                    return;
                }
            } else {
                assert event.getSubcommandName() != null;
                switch (event.getSubcommandName()) {
                    case "youtube" -> result = "ytsearch:" + searchOption.getAsString();
                    case "soundcloud" -> result = "scsearch:" + searchOption.getAsString();
                    case "spotify" -> result = "spsearch:" + searchOption.getAsString();
                }
            }
        }

        if (memberVoiceState.getChannel() == null) {
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            manager.openConnection((VoiceChannel) memberVoiceState.getChannel());
        } else if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        AudioLoadHandler.loadAndPlay(manager, result, event);
    }
}
