package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;
import org.virep.jdabot.utils.ErrorManager;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BannerCommand implements SlashCommand {
    @Override
    public String getName() {
        return "banner";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get the banner of a user.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Récupèrer la bannière de quelqu'un.")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user you want to see the banner")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre auquel vous voulez voir la bannière"),
                        new OptionData(OptionType.BOOLEAN, "color", "Shows the color of the banner.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Montre la couleur par défaut de la bannière.")
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
        Guild guild = event.getGuild();

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
                    .setAuthor(user.getEffectiveName(), null, user.getAvatarUrl())
                    .setTimestamp(Instant.now());

            if (!color) {
                if (jsonObject.get("link") == null) {
                    event.reply(Language.getString("BANNER_NOBANNER", guild)).queue();
                    return;
                }
                embedBuilder.setTitle(Language.getString("BANNER_BANNERTITLE", guild).replace("%USERTAG%", user.getEffectiveName()));
                embedBuilder.setDescription(Language.getString("BANNER_BANNERLINK", guild).replace("%BANNERLINK%", jsonObject.getString("link")));
                embedBuilder.setImage(jsonObject.getString("link") + "?size=2048");
            } else {
                if (jsonObject.getString("color") == null) {
                    event.reply(Language.getString("BANNER_NOCOLOR", guild)).queue();
                    return;
                }
                embedBuilder.setTitle(Language.getString("BANNER_COLORTITLE", guild).replace("%USERTAG%", user.getEffectiveName()).replace("%COLORSTRING%", jsonObject.getString("color")));
                embedBuilder.setColor(Integer.parseInt(jsonObject.getString("color").substring(1), 16));
                embedBuilder.setDescription(Language.getString("BANNER_COLORLINK", guild).replace("%COLOR%", jsonObject.getString("color").substring(1)));
                embedBuilder.setImage("https://singlecolorimage.com/get/" + jsonObject.getString("color").substring(1) + "/1100x440");

            }

            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
