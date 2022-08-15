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
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("bite", "Bite someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("blush", "Blush at someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("lick", "Lick someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("poke", "Poke someone.")
                                .addOption(OptionType.USER, "user", "The user you want to interact with."),
                        new SubcommandData("smile", "Smile at someone.")
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
        descriptions.put("cuddle", "**%author% cuddles %user% !**");
        descriptions.put("slap", "**%author% slapped %user% !**");
        descriptions.put("pat", "**%user% got a pat by %author% !**");
        descriptions.put("feed", "**%user% got feeded by %author% !**");
        descriptions.put("hug", "**%author% gives a hug to %user% !**");
        descriptions.put("kiss", "**%author% gives a kiss to %user% !**");
        descriptions.put("tickle", "**%author% tickled %user% !**");
        descriptions.put("bite", "**%author% bit %user% !**");
        descriptions.put("blush", "**%user% made %author% blush !**");
        descriptions.put("lick", "**%author% licked %user% !**");
        descriptions.put("poke", "**%author% poked %user% !**");
        descriptions.put("smile", "**%author% smiled at %user% !**");


        String endpoint = event.getSubcommandName();
        String author = event.getUser().getAsMention();
        String user = event.getOption("user") != null ? Objects.requireNonNull(event.getOption("user")).getAsUser().getAsMention() : event.getJDA().getSelfUser().getAsMention();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            Request request = new Request.Builder()
                    .url("https://purrbot.site/api/img/sfw/" + endpoint + "/gif")
                    .build();

            Response res = client.newCall(request).execute();

            ResponseBody body = res.body();
            assert body != null;

            JSONObject jsonObject = new JSONObject(body.string());
            String url = jsonObject.getString("link");

            MessageEmbed embed = new EmbedBuilder()
                    .setDescription(descriptions.getString(endpoint).replace("%author%", author).replace("%user%", user))
                    .setImage(url)
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
