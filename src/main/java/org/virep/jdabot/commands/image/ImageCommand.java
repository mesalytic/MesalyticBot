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
                        new SubcommandData("circle", "circle image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("blur", "blur image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("deepfry", "deepfry image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("3000years", "3000years image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("beautiful", "beautiful image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("bobross", "bobross image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("distort", "distort image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("fire", "fire image edit")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "Use the avatar of a user"),
                                        new OptionData(OptionType.STRING, "url", "Edit any specified image url."),
                                        new OptionData(OptionType.ATTACHMENT, "attachment", "Edit any attached image.")),
                        new SubcommandData("fractal", "generate fractal")
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

        try {
            Request request = new Request.Builder()
                    .url("https://api.mesavirep.xyz/" + endpoint + "?url=" + url+"?size=256")
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
