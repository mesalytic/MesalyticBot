package org.virep.jdabot.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.io.FileInputStream;
import java.io.IOException;

public class ActivityCommand extends SlashCommand {
    public ActivityCommand() {
        super("activity", "Launch a voice channel activity.", false, new OptionData[] {
                new OptionData(OptionType.STRING, "application", "Choose the activity you want !", true).addChoices(
                        new Command.Choice("Watch Together", "880218394199220334"),
                        new Command.Choice("Sketch Heads", "902271654783242291"),
                        new Command.Choice("Word Snacks", "879863976006127627"),
                        new Command.Choice("Ask Away", "976052223358406656"),
                        new Command.Choice("Know What I Meme", "950505761862189096"),
                        new Command.Choice("Bobble League (Boost Level 1)", "947957217959759964"),
                        new Command.Choice("Putt Party (Boost Level 1)", "945737671223947305"),
                        new Command.Choice("Chess In The Park (Boost Level 1)", "832012774040141894"),
                        new Command.Choice("Poker Night (Boost Level 1)", "755827207812677713"),
                        new Command.Choice("Letter League (Boost Level 1)", "879863686565621790"),
                        new Command.Choice("SpellCast (Boost Level 1)", "852509694341283871"),
                        new Command.Choice("Checkers In The Park (Boost Level 1)", "832013003968348200"),
                        new Command.Choice("Blazing 8s (Boost Level 1)", "832025144389533716"),
                        new Command.Choice("Land-io (Boost Level 1)", "903769130790969345")
                        )
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.reply("You must be in a voice channel.").setEphemeral(true).queue();
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
            e.printStackTrace();
        }

        // Is deprecated, must find replacement for RequestBody#create();
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        try {
            Request request = new Request.Builder()
                    .url("https://discord.com/api/v10/channels/" + event.getMember().getVoiceState().getChannel().getId() + "/invites")
                    .post(body)
                    .addHeader("Authorization", event.getJDA().getToken())
                    .build();

            FileInputStream in = new FileInputStream("./activities.json");

            JSONTokener tokener = new JSONTokener(in);
            JSONObject activitiesObject = new JSONObject(tokener);

            if (!activitiesObject.has(event.getOption("application").getAsString())) {
                event.reply("This activity somehow doesn't exist, please report the error.").queue();
                return;
            }

            JSONObject activityObject = activitiesObject.getJSONObject(event.getOption("application").getAsString());

            if (event.getGuild().getBoostTier().getKey() != activityObject.getInt("boostLevel")) {
                event.reply("You must be at least Boost Level 1 to start this event.").setEphemeral(true).queue();
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
                    "Required Nitro Boost Level: " +
                    activityObject.get("boostLevel") +
                    "\n" +
                    "Max Participants: " +
                    (activityObject.get("maxParticipants").equals(-1) ? "Unlimited": activityObject.get("maxParticipants")) +
                    "\n\n" +
                    "__**You must be on PC.**__" +
                    "\n" +
                    "[Click here to access the activity !](https://discord.gg/" +
                    inviteCode +
                    ")";

            MessageEmbed embed = new EmbedBuilder()
                    .setThumbnail("https://cdn.discordapp.com/app-icons/" + event.getOption("application").getAsString() + "/" + applicationIcon + ".webp")
                    .setTitle(applicationJsonObject.getString("name"))
                    .setDescription(description)
                    .build();

            event.replyEmbeds(embed).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
