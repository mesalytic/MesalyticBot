package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;
import org.virep.jdabot.utils.ErrorManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EmojisCommand implements SlashCommand {
    @Override
    public String getName() {
        return "emojis";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Manages your emojis in easy way!")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_GUILD_EXPRESSIONS))
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Gérez vos emojis facilement!")
                .addSubcommands(
                        new SubcommandData("add", "Add any emojis you want!")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoutez les emojis que vous voulez!")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "name", "The name of the emoji", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le nom de l'emoji"),
                                        new OptionData(OptionType.STRING, "emoji", "The external custom emoji you want to add.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'emoji venant d'un autre serveur que vous souhaitez ajouter."),
                                        new OptionData(OptionType.ATTACHMENT, "file", "The image file that contains the emoji.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'image qui contient l'emoji.")
                                ),
                        new SubcommandData("remove", "Remove any emojis you want !")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retirez les emojis que vous voulez !")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "emoji", "The emoji you want to remove", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'emoji que vous souhaitez retirer")
                                )
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MANAGE_GUILD_EXPRESSIONS);

        return perms;
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName().equals("add")) {
            String name = event.getOption("name", OptionMapping::getAsString);

            if (event.getOption("emoji") == null && event.getOption("file") == null) {
                event.reply(Language.getString("EMOJIS_NOOPTION", guild)).setEphemeral(true).queue();
                return;
            }

            if (event.getOption("emoji", OptionMapping::getAsString) != null) {
                EmojiUnion formatted = Emoji.fromFormatted(event.getOption("emoji", OptionMapping::getAsString));
                Emoji.Type type = formatted.getType();

                if (type == Emoji.Type.UNICODE) {
                    event.reply(Language.getString("EMOJIS_UNICODE", guild)).setEphemeral(true).queue();
                    return;
                }

                try {
                    CustomEmoji emoji = formatted.asCustom();

                    guild.createEmoji(name, Icon.from(emoji.getImage().download().get())).queue(createdEmoji -> {
                        event.reply(Language.getString("EMOJIS_ADDED", guild).replaceAll("%FORMATTED%", createdEmoji.getFormatted())).queue();
                    });
                } catch (IOException | ExecutionException | InterruptedException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (event.getOption("file", OptionMapping::getAsAttachment) != null) {

                Message.Attachment file = event.getOption("file", OptionMapping::getAsAttachment);

                if (!file.getContentType().equals("image/png") || !file.getContentType().equals("image/gif") || !file.getContentType().equals("image/jpeg")) {
                    event.reply(Language.getString("EMOJIS_INVALID", guild)).setEphemeral(true).queue();
                    return;
                }

                try {
                    guild.createEmoji(name, Icon.from(file.getProxy().download().get())).queue(createdEmoji -> {
                        event.reply(Language.getString("EMOJIS_ADDED", guild).replaceAll("%FORMATTED%", createdEmoji.getFormatted())).queue();
                    });
                } catch (IOException | ExecutionException | InterruptedException | ErrorResponseException e) {
                    ErrorManager.handle(e, event);
                }
            }
        } else {
            EmojiUnion formatted = Emoji.fromFormatted(event.getOption("emoji", OptionMapping::getAsString));
            Emoji.Type type = formatted.getType();

            if (type == Emoji.Type.UNICODE) {
                event.reply(Language.getString("EMOJIS_UNICODE", guild)).setEphemeral(true).queue();
                return;
            }

            CustomEmoji emoji = formatted.asCustom();
            RichCustomEmoji guildEmoji = guild.getEmojiById(emoji.getId());

            if (guildEmoji.isManaged()) {
                event.reply(Language.getString("EMOJIS_MANAGED", guild)).setEphemeral(true).queue();
                return;
            }

            guildEmoji.delete().queue(deletedEmoji -> {
                event.reply(Language.getString("EMOJIS_DELETED", guild).replace("%NAME%", guildEmoji.getName())).queue();
            });
        }
    }
}
