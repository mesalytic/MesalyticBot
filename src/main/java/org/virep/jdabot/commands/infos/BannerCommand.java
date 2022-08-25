package org.virep.jdabot.commands.infos;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.virep.jdabot.slashcommandhandler.Command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class BannerCommand implements Command {
    @Override
    public String getName() {
        return "banner";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get the banner of a user (either global or guild-specific).")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user you want to see the banner"),
                        new OptionData(OptionType.BOOLEAN, "color", "Shows the color of the banner.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping userMapping = event.getOption("user");
        OptionMapping colorMapping = event.getOption("color");

        User user = userMapping != null ? userMapping.getAsUser() : event.getUser();
        boolean color = colorMapping != null && colorMapping.getAsBoolean();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://discordlookup.mesavirep.xyz/" + user.getId())
                    .build();

            Response res = client.newCall(request).execute();

            assert res.body() != null;

            JSONObject jsonObject = new JSONObject(res.body().string()).getJSONObject("banner");

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                    .setTimestamp(Instant.now());

            if (!color) {
                if (jsonObject.getString("link") == null) {
                    event.reply("This user does not have a banner. Try using the `/banner color:true` parameter.").queue();
                    return;
                }
                embedBuilder.setTitle("**" + user.getAsTag() + "**'s banner is:");
                embedBuilder.setDescription("[Link to the banner](" + jsonObject.getString("link") + "?size=2048" + ")");
                embedBuilder.setImage(jsonObject.getString("link") + "?size=2048");
            } else {
                if (jsonObject.getString("color") == null) {
                    event.reply("This user does not have a banner color.").queue();
                    return;
                }
                embedBuilder.setTitle("**" + user.getAsTag() + "**'s banner color is: " + jsonObject.getString("color"));
                embedBuilder.setColor(Integer.parseInt(jsonObject.getString("color").substring(1), 16));
                embedBuilder.setDescription("[Link to the banner](https://singlecolorimage.com/get/" + jsonObject.getString("color").substring(1) + "/1100x440)");
                embedBuilder.setImage("https://singlecolorimage.com/get/" + jsonObject.getString("color").substring(1) + "/1100x440");

            }

            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
