package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.virep.jdabot.utils.Utils.getRandomColor;

public class ColorCommand implements Command {
    @Override
    public String getName() {
        return "color";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get informations about a color (specific or random)")
                .addOption(OptionType.STRING, "color", "Specify a specific color (name or hex code)");
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String hexCode = event.getOption("color") != null ? event.getOption("color", OptionMapping::getAsString).replace("#", "") : getRandomColor();

        Pattern color = Pattern.compile("[0-9A-Fa-f]+$");
        Matcher matcher = color.matcher(hexCode);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://api.mesavirep.xyz/v1color?input=" + (matcher.matches() ? String.format("%06x", Integer.parseInt(hexCode, 16)) : hexCode))
                    .build();

            Response res = client.newCall(request).execute();

            assert res.body() != null;

            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(res.body()).string());

            boolean success = jsonObject.getBoolean("success");

            if (!success) {
                ErrorManager.handle(new Exception(jsonObject.getString("message")), event);
                return;
            }

            JSONObject body = jsonObject.getJSONObject("body");

            String hsl = body.getString("hsl");
            String cmyk = body.getString("cmyk");
            String name = body.getString("name");
            String xyz = body.getString("xyz");
            String hex = body.getString("hex");
            String hsv = body.getString("hsv");
            String rgb = body.getString("rgb");

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(name)
                    .setDescription("[Link to color](https://www.colorhexa.com/"+ hex + ")")
                    .setColor(Color.decode(hex))
                    .setThumbnail("https://www.colorhexa.com/" + hex.substring(1) + ".png")
                    .addField("Hex", hex, true)
                    .addField("RGB", rgb, true)
                    .addField("HSL", hsl, true)
                    .addField("HSV", hsv, true)
                    .addField("CMYK", cmyk, true)
                    .addField("XYZ", xyz, true)
                    .build();

            event.getHook().editOriginalEmbeds(embed).queue();
        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
