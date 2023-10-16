package org.virep.jdabot.commands.music;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.music.AudioLoadHandler;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.handlers.SlashCommand;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PlayCommand implements SlashCommand {

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Play music on any voice channel.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Joue de la musique sur n'importe quel salon vocal.")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("url", "Play songs from any specified URL.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Joue de la musique a partir de n'importe quel lien")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "url", "Specified URL containing music", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Lien spécifié contenant de la musique")
                                ),
                        new SubcommandData("search", "Search songs on SoundCloud or Deezer.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Cherche de la musique sur SoundCloud ou Deezer.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "query", "Search Query", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Contenu de la recherche"),
                                        new OptionData(OptionType.STRING, "platform", "Platform to search the Query", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Plateforme sur la quelle faire la recherche")
                                                .addChoices(
                                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("SoundCloud", "soundcloud"),
                                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Deezer", "deezer")
                                                )
                                ),
                        new SubcommandData("file", "Play any music file from your computer !")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Joue n'importe quel fichier audio disponible sur votre appareil !")
                                .addOptions(
                                        new OptionData(OptionType.ATTACHMENT, "file", "The audio file to play.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le fichier audio a jouer.")
                                )
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
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
        event.deferReply().queue();
        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(event.getGuild());

        Guild guild = event.getGuild();

        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;

        String result = "";

        if (event.getSubcommandName().equals("url")) {
            String url = event.getOption("url", OptionMapping::getAsString);
            if (url.contains("krakenfiles")) {
                try {
                    Document doc = Jsoup.connect(url).get();
                    Element audio = doc.select("#jp_container_1 > div > div.sfPlayer_wave > div.sfPlayer_tabWav > div > div.jp-progress > div > img").first();

                    url = "https://" + audio.attr("src").replace("//", "").replace("waveform.png", "music.m4a");

                    new URL(url);
                    result = url;
                } catch (IOException e) {
                    event.getHook().editOriginal(Language.getString("PLAY_NOTVALIDURL", guild)).queue();
                    return;
                }
            } else try {
                new URL(url);
                result = url;
            } catch (MalformedURLException e) {
                event.getHook().editOriginal(Language.getString("PLAY_NOTVALIDURL", guild)).queue();
                return;
            }
        }

        if (event.getSubcommandName().equals("search")) {
            String query = event.getOption("query", OptionMapping::getAsString);
            String platform = event.getOption("platform", OptionMapping::getAsString);

            switch (platform) {
                case "deezer":
                    result = "dzsearch:" + query;
                    break;
                case "soundcloud":
                    result = "scsearch:" + query;
                    break;
            }
        }

        if (event.getSubcommandName().equals("file")) {
            Attachment attachment = event.getOption("file", OptionMapping::getAsAttachment);

            result = attachment.getProxyUrl();
        }

        if (memberVoiceState.getChannel() == null) {
            event.getHook().editOriginal(Language.getString("MUSIC_NOVOICECHANNEL", guild)).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            manager.openConnection(memberVoiceState.getChannel());
        } else if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.getHook().editOriginal(Language.getString("MUSIC_NOTSAMEVC", guild)).queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        AudioLoadHandler.loadAndPlay(manager, result, event, memberVoiceState.getChannel().getType());
    }
}