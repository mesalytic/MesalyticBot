package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.slashcommandhandler.Command;

public class AvatarCommand implements Command {
    @Override
    public String getName() {
        return "avatar";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays the avatar for the specified user.")
                .addOption(OptionType.USER, "user", "The user to display.")
                .addOption(OptionType.BOOLEAN, "guild", "Display guild profile picture");
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

        event.reply("Here is **" + member.getUser().getAsTag() + "** avatar:\n\n" + avatar).queue();
    }
}
