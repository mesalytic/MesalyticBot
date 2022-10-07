package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvatarCommand implements Command {
    @Override
    public String getName() {
        return "avatar";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays the avatar for the specified user.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Affiche votre avatar ou celui d'un membre")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user to display.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le membre a afficher l'avatar"),
                        new OptionData(OptionType.BOOLEAN, "guild", "Display guild profile picture")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Afficher l'avatar serveur.")
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
        OptionMapping userOption = event.getOption("user");
        Member member = userOption != null ? userOption.getAsMember() : event.getMember();
        boolean guild = event.getOption("guild") != null ? event.getOption("guild", OptionMapping::getAsBoolean) : false;

        String avatar = guild ? member.getEffectiveAvatarUrl() : member.getUser().getAvatarUrl();

        event.reply(avatar).queue();
    }
}
