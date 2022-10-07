package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Débannir un utilisateur.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addOptions(
                        new OptionData(OptionType.STRING, "user", "The user to unban. (User tag or user ID)", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'utilisateur à débannir. (tag ou ID)"),
                        new OptionData(OptionType.STRING, "reason", "The reason of the unban")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La raison du débannissement")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MODERATE_MEMBERS, Permission.BAN_MEMBERS, Permission.VIEW_AUDIT_LOGS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        String user = event.getOption("user", OptionMapping::getAsString);
        String reason = event.getOption("reason", OptionMapping::getAsString);


        Matcher tagMatcher = User.USER_TAG.matcher(user);

        if (tagMatcher.find()) {
            guild.retrieveBanList().queue(bans -> {
                Optional<Guild.Ban> ban = bans.stream().filter(banSearch -> banSearch.getUser().getAsTag().equals(user)).findFirst();

                if (ban.isPresent()) {
                    Guild.Ban banObject = ban.get();
                    UserSnowflake userID = UserSnowflake.fromId(banObject.getUser().getId());

                    guild.unban(userID).reason(Language.getString("UNBAN_UNBANNEDBY", guild).replace("%USERTAG%", event.getUser().getAsTag()).replace("%REASON%", (reason != null ? reason : Language.getString("UNBAN_NOREASON", guild)))).queue();
                    event.reply(Language.getString("UNBAN_UNBANNED", guild).replace("%USERTAG%", banObject.getUser().getAsTag())).queue();
                } else {
                    event.reply(Language.getString("UNBAN_NOTVALID", guild)).setEphemeral(true).queue();
                }
            });
        } else if ((user.length() >= 17 && user.length() <= 20) && Helpers.isNumeric(user)) {
            guild.retrieveBanList().queue(bans -> {
                Optional<Guild.Ban> ban = bans.stream().filter(banSearch -> banSearch.getUser().getId().equals(user)).findFirst();

                if (ban.isPresent()) {
                    Guild.Ban banObject = ban.get();

                    guild.unban(UserSnowflake.fromId(user)).reason(Language.getString("UNBAN_UNBANNEDBY", guild).replace("%USERTAG%", event.getUser().getAsTag()).replace("%REASON%", (reason != null ? reason : Language.getString("UNBAN_NOREASON", guild)))).queue();
                    event.reply(Language.getString("UNBAN_UNBANNED", guild).replace("%USERTAG%", banObject.getUser().getAsTag())).queue();
                } else {
                    event.reply(Language.getString("UNBAN_NOTVALID", guild)).setEphemeral(true).queue();
                }
            });
        } else {
            event.reply(Language.getString("UNBAN_NOTUSER", guild)).setEphemeral(true).queue();
        }
    }
}
