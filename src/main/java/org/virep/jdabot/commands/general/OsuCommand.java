package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OsuCommand implements Command {
    @Override
    public String getName() {
        return "osu";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Gets stats from Osu!")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Obtenez des stats venant d'osu!")
                .addOptions(
                        new OptionData(OptionType.STRING, "username", "The username of the profile", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le nom d'utilisateur du profil"),
                        new OptionData(OptionType.STRING, "mode", "The game mode.", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le mode de jeu")
                                .addChoices(
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Standard", "osu"),
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Taiko", "taiko"),
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Mania", "mania"),
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Catch The Beat", "ctb")
                                )
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
        String username = Objects.requireNonNull(event.getOption("username")).getAsString();
        String mode = Objects.requireNonNull(event.getOption("mode")).getAsString();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://api.mesavirep.xyz/v1/osu?user=" + username + "&mode=" + mode)
                    .build();

            Response res = client.newCall(request).execute();

            assert res.body() != null;

            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(res.body()).string());
            JSONArray jsonBody = jsonObject.getJSONObject("body").getJSONArray("data");

            byte[] bytes = new byte[jsonBody.length()];

            for (int i = 0; i < jsonBody.length(); i++) {
                bytes[i] = (byte) (((int) jsonBody.get(i)) & 0xFF);
            }

            InputStream targetBytes = new ByteArrayInputStream(bytes);

            FileUpload file = AttachedFile.fromData(targetBytes, "osu-" + username + jsonObject.get("ext"));

            event.getHook().editOriginalAttachments(file).queue();
        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
