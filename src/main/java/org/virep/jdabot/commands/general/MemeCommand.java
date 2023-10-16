package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.virep.jdabot.handlers.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemeCommand implements Command {
    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Returns a random meme from Reddit.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Envoie un meme aléatoire venant de Reddit.")
                .addOptions(
                        new OptionData(OptionType.STRING, "subreddit", "The subreddit you want to see memes from.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le subreddit auquel vous voulez voir des memes")
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
        String[] subreddits = {"dankmemes", "memes", "crappyoffbrands", "MemeEconomy", "me_irl"};

        OptionMapping subredditOption = event.getOption("subreddit");

        String subreddit = subredditOption != null ? subredditOption.getAsString() : subreddits[(int) Math.floor(Math.random() * subreddits.length)];

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://www.reddit.com/r/" + subreddit + "/top.json?sort=top&t=day&limit=500")
                    .build();

            Response res = client.newCall(request).execute();

            assert res.body() != null;

            JSONObject jsonObject = new JSONObject(res.body().string());

            JSONArray childrens = jsonObject.getJSONObject("data").getJSONArray("children");
            JSONObject children = childrens.getJSONObject((int) Math.floor(Math.random() * childrens.length())).getJSONObject("data");

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(children.getString("title"), "https://reddit.com" + children.getString("permalink"))
                    .setImage(children.getString("url"))
                    .setColor(0x9590EE)
                    .setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl())
                    .setFooter("\uD83D\uDC4D " + children.getInt("ups") + " | \uD83D\uDC4E " + children.getInt("downs") + " | r/" + children.getString("subreddit"))
                    .build();


            event.replyEmbeds(embed).queue();
        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
