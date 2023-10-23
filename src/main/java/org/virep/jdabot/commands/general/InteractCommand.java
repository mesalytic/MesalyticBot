package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;
import org.virep.jdabot.utils.ErrorManager;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InteractCommand implements SlashCommand {
    @Override
    public String getName() {
        return "interact";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Interact with others !")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Interagis avec les autres !")
                .addSubcommands(
                        new SubcommandData("cuddle", "Cuddle someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Caline quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("slap", "Slap someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Donne une claque a quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("pat", "Pat someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Caresse quelqu'un sur la tête.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("feed", "Feed someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Nourris quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("hug", "Give a hug to someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Fait un calin à quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("kiss", "Give a kiss to someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Embrasse quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("tickle", "Tickle someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Chatouille quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("bite", "Bite someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Mords quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("blush", "Blush at someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Rougis à quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("lick", "Lick someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Lèche quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("poke", "Poke someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Donne un petit coup à quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
                                ),
                        new SubcommandData("smile", "Smile at someone.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Souris à quelqu'un.")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The user you want to interact with.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre avec qui vous voulez intéragir")
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
        Guild guild = event.getGuild();

        JSONObject descriptions = new JSONObject();
        descriptions.put("cuddle", Language.getString("INTERACT_CUDDLE", guild));
        descriptions.put("slap", Language.getString("INTERACT_SLAP", guild));
        descriptions.put("pat", Language.getString("INTERACT_PAT", guild));
        descriptions.put("feed", Language.getString("INTERACT_FEED", guild));
        descriptions.put("hug", Language.getString("INTERACT_HUG", guild));
        descriptions.put("kiss", Language.getString("INTERACT_KISS", guild));
        descriptions.put("tickle", Language.getString("INTERACT_TICKLE", guild));
        descriptions.put("bite", Language.getString("INTERACT_BITE", guild));
        descriptions.put("blush", Language.getString("INTERACT_BLUSH", guild));
        descriptions.put("lick", Language.getString("INTERACT_LICK", guild));
        descriptions.put("poke", Language.getString("INTERACT_POKE", guild));
        descriptions.put("smile", Language.getString("INTERACT_SMILE", guild));


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
            ErrorManager.handle(e, event);
        }
    }
}
