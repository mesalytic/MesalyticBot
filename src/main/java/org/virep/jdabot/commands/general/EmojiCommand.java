package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;
import org.virep.jdabot.utils.Twemoji;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EmojiCommand implements Command {
    @Override
    public String getName() {
        return "emoji";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays informations about a emoji!")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche des informations a propos d'un emoji.")
                .addOptions(
                        new OptionData(OptionType.STRING, "emoji", "The emoji you want to see informations about.", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'emoji auquel vous souhaitez voir les informations")
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

        EmojiUnion formatted = Emoji.fromFormatted(event.getOption("emoji", OptionMapping::getAsString));
        Emoji.Type type = formatted.getType();

        try {
            switch(type) {
                case CUSTOM -> {
                    CustomEmoji emoji = formatted.asCustom();

                    event.reply(Language.getString("EMOJI_CUSTOMPREVIEW", guild).replace("%EMOJINAME%", emoji.getName()).replaceAll("%FORMATTED%", guild.getEmojiById(emoji.getId()) != null ? emoji.getFormatted() : "N/A").replace("%EMOJIID%", emoji.getId()).replace("%SECONDS%", String.valueOf(emoji.getTimeCreated().toEpochSecond()))).setFiles(FileUpload.fromData(emoji.getImage().download().get(), emoji.isAnimated() ? "emoji.gif" : "emoji.png")).queue();
                }

                case UNICODE -> {
                    UnicodeEmoji emoji = formatted.asUnicode();

                    String emojiURL = Twemoji.parseOne(formatted.getFormatted());

                    if (emojiURL == null) {
                        event.reply(Language.getString("EMOJI_ERROR", guild)).setEphemeral(true).queue();
                        return;
                    }

                    InputStream emojiStream = new URL(emojiURL).openStream();

                    event.reply(Language.getString("EMOJI_UNICODEPREVIEW", guild).replace("%FORMATTED%", emoji.getFormatted()).replace("%CODEPOINTS%", emoji.getAsCodepoints())).setFiles(FileUpload.fromData(emojiStream, "emoji.png")).queue();
                }
            }
        } catch (ExecutionException | InterruptedException | IOException e) {
            ErrorManager.handle(e, event);
        }
    }
}
