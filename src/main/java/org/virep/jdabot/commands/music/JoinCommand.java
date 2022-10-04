package org.virep.jdabot.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Objects;

public class JoinCommand implements Command {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Join channel to play music.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Rejoins un salon pour jouer de la musique.")
                .setGuildOnly(true);
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        GuildVoiceState voiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert voiceState != null;
        if (voiceState.getChannel() == null) {
            event.reply(Language.getString("MUSIC_NOVOICECHANNEL", guild)).setEphemeral(true).queue();
            return;
        }
        assert guild != null;
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();

        if (selfVoiceState != null && selfVoiceState.inAudioChannel()) {
            event.reply(Language.getString("JOIN_ALREADYJOINED", guild)).setEphemeral(true).queue();
            return;
        }
        VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceState.getChannel().getIdLong());

        assert voiceChannel != null;
        manager.openConnection(voiceChannel);

        event.reply(Language.getString("JOIN_JOINED", guild).replace("%CHANNELNAME%", voiceChannel.getName())).queue();
    }
}