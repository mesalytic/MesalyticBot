package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import org.virep.jdabot.slashcommandhandler.Command;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InteractCommand implements Command {
    @Override
    public String getName() {
        return "interact";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Interact with others")
                .addSubcommands(
                        new SubcommandData("smug", "Smug at someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("cuddle", "Cuddle someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("slap", "Slap someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("pat", "Pat someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("feed", "Feed someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("hug", "Give a hug to someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("kiss", "Give a kiss to someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("tickle", "Tickle someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        JSONObject descriptions = new JSONObject();
        descriptions.put("smug", "got smugged by");
        descriptions.put("cuddle", "got cuddled by");
        descriptions.put("slap", "got slapped by");
        descriptions.put("pat", "got pat by");
        descriptions.put("feed", "got feeded by");
        descriptions.put("hug", "got a hug from");
        descriptions.put("kiss", "got a kiss from");
        descriptions.put("tickle", "got tickled by");

        String endpoint = event.getSubcommandName();
        String author = event.getUser().getAsMention();
        String user = event.getOption("user") != null ? Objects.requireNonNull(event.getOption("user")).getAsUser().getAsMention() : event.getJDA().getSelfUser().getAsMention();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://nekos.life/api/v2/img/" + endpoint)
                    .build();

            Response res = client.newCall(request).execute();

            ResponseBody body = res.body();
            assert body != null;

            JSONObject jsonObject = new JSONObject(body.string());
            String url = jsonObject.getString("url");

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("**" + user + " " + descriptions.get(endpoint) + " " + author + " !**")
                    .setImage(url)
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
