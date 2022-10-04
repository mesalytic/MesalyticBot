package org.virep.jdabot.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ActivityCommand implements Command {
    @Override
    public String getName() {
        return "activity";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Launch a voice channel activity.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Lance une activité dans un salon vocal.")
                .setGuildOnly(true)
                .addOptions(new OptionData(OptionType.STRING, "application", "Choose the activity you want !", true)
                        .setDescriptionLocalization(DiscordLocale.FRENCH, "Choisissez l'activité que vous voulez !")
                        .addChoices(
                                new Choice("Watch Together", "880218394199220334"),
                                new Choice("Sketch Heads", "902271654783242291"),
                                new Choice("Word Snacks", "879863976006127627"),
                                new Choice("Ask Away", "976052223358406656"),
                                new Choice("Know What I Meme", "950505761862189096"),
                                new Choice("Bobble League (Boost Level 1)", "947957217959759964"),
                                new Choice("Putt Party (Boost Level 1)", "945737671223947305"),
                                new Choice("Chess In The Park (Boost Level 1)", "832012774040141894"),
                                new Choice("Poker Night (Boost Level 1)", "755827207812677713"),
                                new Choice("Letter League (Boost Level 1)", "879863686565621790"),
                                new Choice("SpellCast (Boost Level 1)", "852509694341283871"),
                                new Choice("Checkers In The Park (Boost Level 1)", "832013003968348200"),
                                new Choice("Blazing 8s (Boost Level 1)", "832025144389533716"),
                                new Choice("Land-io (Boost Level 1)", "903769130790969345"),
                                new Choice("Bash Out (Boost Level 1)", "1006584476094177371")
                ));
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.reply(Language.getString("NOVOICECHANNEL", guild)).setEphemeral(true).queue();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("max_age", "0");
            jsonObject.put("target_application_id", event.getOption("application").getAsString());
            jsonObject.put("target_type", "2");
        } catch (JSONException e) {
            ErrorManager.handle(e, event);
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        try {
            Request request = new Request.Builder()
                    .url("https://discord.com/api/v10/channels/" + event.getMember().getVoiceState().getChannel().getId() + "/invites")
                    .post(body)
                    .addHeader("Authorization", event.getJDA().getToken())
                    .build();

            InputStream in = ActivityCommand.class.getResourceAsStream("/activities.json");

            JSONTokener tokener = new JSONTokener(in);
            JSONObject activitiesObject = new JSONObject(tokener);

            if (!activitiesObject.has(event.getOption("application").getAsString())) {
                event.reply(Language.getString("ACTIVITY_NOEXIST", guild)).queue();
                return;
            }

            JSONObject activityObject = activitiesObject.getJSONObject(event.getOption("application").getAsString());

            if (guild.getBoostTier().getKey() < activityObject.getInt("boostLevel")) {
                event.reply(Language.getString("ACTIVITY_BOOSTREQUIRED", guild)).setEphemeral(true).queue();
                return;
            }

            Response res = client.newCall(request).execute();

            assert res.body() != null;
            JSONObject jsonBody = new JSONObject(res.body().string());

            JSONObject applicationJsonObject = jsonBody.getJSONObject("target_application");
            String applicationIcon = applicationJsonObject.getString("icon");

            String inviteCode = jsonBody.getString("code");
            String description = applicationJsonObject.get("description") +
                    "\n\n" +
                    Language.getString("ACTIVITY_EMBED_REQUIRED", guild) +
                    activityObject.get("boostLevel") +
                    "\n" +
                    Language.getString("ACTIVITY_EMBED_MAX", guild) +
                    (activityObject.get("maxParticipants").equals(-1) ? Language.getString("ACTIVITY_EMBED_MAX_UNLIMITED", guild): activityObject.get("maxParticipants")) +
                    "\n\n" +
                    Language.getString("ACTIVITY_EMBED_PC", guild) +
                    "\n" +
                    "[" +
                    Language.getString("ACTIVITY_EMBED_ACCESS", guild) +
                    "](https://discord.gg/" +
                    inviteCode +
                    ")";

            MessageEmbed embed = new EmbedBuilder()
                    .setThumbnail("https://cdn.discordapp.com/app-icons/" + event.getOption("application").getAsString() + "/" + applicationIcon + ".webp")
                    .setTitle(applicationJsonObject.getString("name"))
                    .setDescription(description)
                    .build();

            event.replyEmbeds(embed).queue();
        } catch (IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
