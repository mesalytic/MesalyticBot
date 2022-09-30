package org.virep.jdabot.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.virep.jdabot.music.AudioLoadHandler;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayCommand implements Command {

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Play music on any voice channel.")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("search", "Search for any songs!")
                                .addOption(OptionType.STRING, "search", "Search string", true),
                        new SubcommandData("soundcloud", "Play SoundCloud songs!")
                                .addOption(OptionType.STRING, "url", "SoundCloud Track or Playlist URL")
                                .addOption(OptionType.STRING, "search", "Search string"),
                        new SubcommandData("url", "Play URLs that are not from Spotify/YouTube/SoundCloud")
                                .addOption(OptionType.STRING, "url", "URL string", true),
                        new SubcommandData("file", "Play the audio files you attach !")
                                .addOption(OptionType.ATTACHMENT, "file", "Audio File", true)
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(event.getGuild());

        Guild guild = event.getGuild();

        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        OptionMapping urlOption = event.getOption("url");
        OptionMapping searchOption = event.getOption("search");
        OptionMapping fileOption = event.getOption("file");

        String result = "";

        if (urlOption == null && searchOption == null && fileOption == null) {
            event.getHook().editOriginal("\u274C - You must select at least an option.").queue();
            return;
        }

        if (urlOption != null && searchOption != null) {
            event.getHook().editOriginal("\u274C - You must select only one option. (either `search` or `url`).").queue();
            return;
        }

        if (event.getSubcommandName().equals("search")) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            try {
                Request request = new Request.Builder()
                        .url("https://api.mesavirep.xyz/v1/music?search=" + URLEncoder.encode(searchOption.getAsString(), StandardCharsets.UTF_8))
                        .build();

                Response res = client.newCall(request).execute();

                assert res.body() != null;

                JSONObject jsonObject = new JSONObject(res.body().string());
                result = jsonObject.getString("body");

            } catch (IOException e) {
                ErrorManager.handle(e, event);
            }
        } else {
            if (urlOption != null) {
                try {
                    new URL(urlOption.getAsString());
                    result = urlOption.getAsString();
                } catch (MalformedURLException e) {
                    event.getHook().editOriginal("\u274C - You must specify a valid URL.").queue();
                    return;
                }
            }

            if (searchOption != null) {
                assert event.getSubcommandName() != null;

                result = "scsearch:" + searchOption.getAsString();
            }

            if (fileOption != null) {
                Attachment attachment = fileOption.getAsAttachment();

                result = attachment.getProxyUrl();
            }
        }

        if (memberVoiceState.getChannel() == null) {
            event.getHook().editOriginal("\u274C - You are not in a voice channel!").queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            manager.openConnection((VoiceChannel) memberVoiceState.getChannel());
        } else if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.getHook().editOriginal("\u274C - You are not in the same channel as me!").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        AudioLoadHandler.loadAndPlay(manager, result, event);
    }
}