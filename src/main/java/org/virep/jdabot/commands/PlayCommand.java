package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.virep.jdabot.lavaplayer.PlayerManager;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayCommand extends SlashCommand {
    public PlayCommand() {
        super("play",
                "Play music on your voice channel!",
                new SubcommandData[] {
                        new SubcommandData("youtube", "Play YouTube urls!").addOption(OptionType.STRING, "url", "YouTube VIDEO URL")
                }
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        assert selfVoiceState != null;
        if (memberVoiceState.getChannel() == null) {
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        if (!selfVoiceState.inAudioChannel()) {
            AudioManager manager = guild.getAudioManager();
            manager.openAudioConnection(memberVoiceState.getChannel());
        } else if (Objects.requireNonNull(selfVoiceState.getChannel()).getIdLong() != memberVoiceState.getChannel().getIdLong()) {
            event.reply("You are not in the same channel as me!").setEphemeral(true).queue();
            return;
        }

        if (!isValidURL(Objects.requireNonNull(event.getOption("url")).getAsString(), "/^(?:https?:\\/\\/)?(?:m\\.|www\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))((\\w|-){11})(?:\\S+)?$/")) {
            event.reply("It is not a valid YouTube VIDEO link!").setEphemeral(true).queue();
            return;
        }

        PlayerManager.getInstance().loadAndPlay(event, Objects.requireNonNull(event.getOption("url")).getAsString());
    }

    private boolean isValidURL(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
