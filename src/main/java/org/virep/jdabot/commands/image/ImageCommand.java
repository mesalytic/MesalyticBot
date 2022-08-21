package org.virep.jdabot.commands.image;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.virep.jdabot.slashcommandhandler.Command;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ImageCommand implements Command {
    @Override
    public String getName() {
        return "image";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Edit any image you want.")
                .addSubcommands(
                        new SubcommandData("circle", "Adds a circle around the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("blur", "Blurs the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("deepfry", "Deepfries the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("3000years", "Add\\'s your (or someone elses) profile pic to the Pokemon Meme `It\\'s been 3000 years...`")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("beautiful", "U see this? Beautiful.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("bobross", "You are now a Bob Ross art.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("distort", "Distorts the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("fire", "Sends a GIF with a fire effect.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("fisheye", "Adds a fisheye effect to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("frame", "Adds a frame in the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("glitch", "Glitches the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("grayscale", "Adds a grayscale effect to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("hearts", "Adds hearts to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("invert", "Inverts the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("pixel", "Pixelize the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("rainbow", "Adds a Rainbow effect to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("sepia", "Adds a sepia effect to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("spotify", "Allows you to create a customized Spotify Now Playing card.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "author", "The custom track author", true),
                                        new OptionData(OptionType.STRING, "title", "The custom track title.", true)),
                        new SubcommandData("steam", "Lets you create a customized `Steam Playing` card.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "game", "The custom game title.", true)),
                        new SubcommandData("triggered", "Sends a GIF with the Triggered effect.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("ussr", "Adds the USSR flag to the image.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("wanted", "Adds the Wanted frame from One Piece to the image")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("fractal", "Generate a fractal. May take some time.")
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String endpoint = event.getSubcommandName();

        OptionMapping userOption = event.getOption("user");
        OptionMapping urlOption = event.getOption("url");
        OptionMapping attachmentOption = event.getOption("attachment");

        String url = userOption != null ? userOption.getAsUser().getAvatarUrl() : urlOption != null ? urlOption.getAsString() : attachmentOption != null ? attachmentOption.getAsAttachment().getUrl() : event.getUser().getAvatarUrl();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiURL = "";

        assert endpoint != null;
        if (endpoint.equals("spotify")) apiURL = "http://localhost:8239/" + endpoint + "?url=" + url + "?size=1024" + "&author=" + event.getOption("author").getAsString() + "&title=" + event.getOption("title").getAsString();
        else if (endpoint.equals("steam")) apiURL = "http://localhost:8239/" + endpoint + "?url=" + url + "?size=1024" + "&game=" + event.getOption("game").getAsString() + "&player=" + event.getUser().getName();
        else apiURL = "http://localhost:8239/" + endpoint + "?url=" + url + "?size=1024";

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
                bytes[i] = (byte)(((int)jsonBody.get(i)) & 0xFF);
            }

            event.getHook().editOriginal(bytes, endpoint + jsonObject.get("ext")).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
