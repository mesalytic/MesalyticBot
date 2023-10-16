package org.virep.jdabot.commands.image;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;
import org.virep.jdabot.utils.ErrorManager;
import org.virep.jdabot.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageCommand implements SlashCommand {
    @Override
    public String getName() {
        return "image";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Edit any image you want.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Modifie n'importe quelle image envoyée.")
                .addSubcommands(
                        new SubcommandData("circle", "Adds a circle around the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Modifie l'image en cercle.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("blur", "Blurs the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Floute l'image")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("deepfry", "Deepfries the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Sature l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("3000years", "Add\\'s your (or someone elses) profile pic to the Pokemon Meme `It\\'s been 3000 years...`")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute l'image sur le meme Pokémon \"It's been 3000 years\"")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("beautiful", "U see this? Beautiful.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("bobross", "You are now a Bob Ross art.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute l'image en tant que peinture de Bob Ross")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("distort", "Distorts the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Distort l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("fire", "Sends a GIF with a fire effect.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Renvoie l'image en tant que GIF avec un effet de feu.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("fisheye", "Adds a fisheye effect to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un filtre FishEye")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("frame", "Adds a frame in the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un cadre autour de l'image")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("glitch", "Glitches the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Corrompt l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("grayscale", "Adds a grayscale effect to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Rajoute un filtre noir et blanc à l'image")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("hearts", "Adds hearts to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un coeur a l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("invert", "Inverts the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Inverse les couleurs de l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("pixel", "Pixelize the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Pixelise l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("rainbow", "Adds a Rainbow effect to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un filtre arc-en-ciel à l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("sepia", "Adds a sepia effect to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un filtre sépia à l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("spotify", "Allows you to create a customized Spotify Now Playing card.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Permets de créer une carte Spotify customisée.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "author", "The custom track author", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'auteur du titre customisé."),
                                        new OptionData(OptionType.STRING, "title", "The custom track title.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le nom du titre customisé.")
                                ),
                        new SubcommandData("steam", "Lets you create a customized `Steam Playing` card.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Permets de créer un statut de jeu Steam customisé.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "game", "The custom game title.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le nom du jeu customisé.")
                                ),
                        new SubcommandData("triggered", "Sends a GIF with the Triggered effect.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Crée un GIF avec l'effet Triggered.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("ussr", "Adds the USSR flag to the image.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un filtre URSS à l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("wanted", "Adds the Wanted frame from One Piece to the image")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un cadre Wanted de One Piece à l'image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utiliser l'avatar d'un autre membre."),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image de l'URL spécifiée"),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Utilise l'image attachée.")
                                ),
                        new SubcommandData("fractal", "Generate a fractal. May take some time.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Génère une fractale. Peut prendre jusqu'a une trentaine de secondes.")
                );
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
        event.deferReply().queue();

        String endpoint = event.getSubcommandName();

        OptionMapping userOption = event.getOption("user");
        OptionMapping urlOption = event.getOption("url");
        OptionMapping attachmentOption = event.getOption("attachment");

        String url = userOption != null ? userOption.getAsUser().getAvatarUrl() : urlOption != null ? urlOption.getAsString() : attachmentOption != null ? attachmentOption.getAsAttachment().getUrl() : event.getUser().getAvatarUrl();

        if (!Utils.isImageUrl(url)) {
            event.getHook().editOriginal(Language.getString("IMAGE_NOTVALID_URL", event.getGuild())).queue();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiURL = "";

        assert endpoint != null;
        if (endpoint.equals("spotify"))
            apiURL = "https://api.mesavirep.xyz/v1/" + endpoint + "?url=" + url + "?size=1024" + "&author=" + event.getOption("author").getAsString() + "&title=" + event.getOption("title").getAsString();
        else if (endpoint.equals("steam"))
            apiURL = "https://api.mesavirep.xyz/v1/" + endpoint + "?url=" + url + "?size=1024" + "&game=" + event.getOption("game").getAsString() + "&player=" + event.getUser().getName();
        else apiURL = "https://api.mesavirep.xyz/v1/" + endpoint + "?url=" + url + "?size=1024";

        try {
            Request request = new Request.Builder()
                    .url(apiURL)
                    .build();

            Response res = client.newCall(request).execute();

            assert res.body() != null;

            JSONObject jsonObject = new JSONObject(res.body().string());
            JSONArray jsonBody = jsonObject.getJSONObject("body").getJSONArray("data");

            byte[] bytes = new byte[jsonBody.length()];

            for (int i = 0; i < jsonBody.length(); i++) {
                bytes[i] = (byte) (((int) jsonBody.get(i)) & 0xFF);
            }

            FileUpload file = AttachedFile.fromData(bytes, endpoint + jsonObject.get("ext"));

            event.getHook().editOriginalAttachments(file).queue();
        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
