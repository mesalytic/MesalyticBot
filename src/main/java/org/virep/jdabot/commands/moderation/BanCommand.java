package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.EnumSet;

//TODO: Change ErrorResponse.UNKNOWN_MEMBER to ErrorResponse.UNKNOWN_USER in JDA 5 alpha.19
//TODO: Change delDays feature to delTime, as Discord API v10 now supports deletion with seconds precision (JDA 5 alpha.19)
public class BanCommand implements Command {
    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Ban a user.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The User to ban.", true),
                        new OptionData(OptionType.STRING, "reason", "The reason of the ban."),
                        new OptionData(OptionType.INTEGER, "delete-days", "Days to delete messages. (0-7)").setMinValue(0).setMaxValue(7)
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }
        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply("The specified member cannot be banned, because of permission discrepancy.").setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.UNKNOWN_MEMBER),
                        (ex) -> event.reply("The specified member cannot be banned, as they are no longer in the server.").setEphemeral(true).queue());

        Member member = event.getOption("user", OptionMapping::getAsMember);
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Banned by " + event.getUser().getAsTag() + ": No reason provided";
        int delDays = event.getOption("delete-days") != null ? event.getOption("delete-days").getAsInt() : 0;

        System.out.println(delDays);
        
        if (member.getId().equals(event.getMember().getId())) {
            event.reply("You cannot ban yourself.").setEphemeral(true).queue();
            return;
        }
        
        if (member.isOwner()) {
            event.reply("The specified member cannot be banned, because they are the server owner.").setEphemeral(true).queue();
            return;
        }

        if (!event.getMember().canInteract(member)) {
            event.reply("The specified member cannot be banned, because of the member has a higher permission position.").setEphemeral(true).queue();
            return;
        }

        member.ban(delDays).reason(reason)
                .queue(success -> event.reply("Member **" + member.getUser().getAsTag() + "** has been banned. (Reason: **" + reason + "**)").queue(), errorHandler);
    }
}
