package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.Optional;
import java.util.regex.Matcher;

public class UnbanCommand implements Command {
    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Unban a user.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.STRING, "user", "The user to unban. (User tag or user ID)", true),
                        new OptionData(OptionType.STRING, "reason", "The reason of the unban")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();

        String user = event.getOption("user", OptionMapping::getAsString);
        String reason = event.getOption("reason", OptionMapping::getAsString);


        Matcher tagMatcher = User.USER_TAG.matcher(user);

        if (tagMatcher.find()) {
            guild.retrieveBanList().queue(bans -> {
                Optional<Guild.Ban> ban = bans.stream().filter(banSearch -> banSearch.getUser().getAsTag().equals(user)).findFirst();

                if (ban.isPresent()) {
                    Guild.Ban banObject = ban.get();
                    UserSnowflake userID = UserSnowflake.fromId(banObject.getUser().getId());

                    guild.unban(userID).reason("Unbanned by " + event.getUser().getAsTag() + ": " + (reason != null ? reason : "No reason provided")).queue();
                    event.reply("The user `" + banObject.getUser().getAsTag() + "` has been unbanned.").queue();
                } else {
                    event.reply("This user is not banned or not valid.").setEphemeral(true).queue();
                }
            });
        } else if ((user.length() >= 17 && user.length() <= 20) && Helpers.isNumeric(user)) {
            guild.retrieveBanList().queue(bans -> {
                Optional<Guild.Ban> ban = bans.stream().filter(banSearch -> banSearch.getUser().getId().equals(user)).findFirst();

                if (ban.isPresent()) {
                    Guild.Ban banObject = ban.get();

                    guild.unban(UserSnowflake.fromId(user)).reason("Unbanned by " + event.getUser().getAsTag() + ": " + (reason != null ? reason : "No reason provided")).queue();
                    event.reply("The user `" + banObject.getUser().getAsTag() + "` has been unbanned.").queue();
                } else {
                    event.reply("This user is not banned or not valid.").setEphemeral(true).queue();
                }
            });
        } else {
            event.reply("Please specify a user TAG (username#0101) or a user ID.").setEphemeral(true).queue();
        }
    }
}
