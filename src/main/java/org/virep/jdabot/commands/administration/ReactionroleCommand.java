package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ReactionroleCommand implements Command {
    @Override
    public String getName() {
        return "reactionrole";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Configure roles that are given when clicking on a reaction.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("add", "Add roles to the reaction role.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that users will interact on.", true),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel that contains the message.", true),
                                        new OptionData(OptionType.ROLE, "role", "The role that users will obtain.", true),
                                        new OptionData(OptionType.STRING, "emoji", "The emoji that users will interact with.", true)
                                ),
                        new SubcommandData("remove", "Removes roles from the reaction role.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that users will interact on.", true),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel that contains the message.", true),
                                        new OptionData(OptionType.STRING, "emoji", "The emoji that users will interact with.", true)
                                )
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName().equals("add")) {
            try {
                EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                Emoji.Type emojiType = fromFormattedEmoji.getType();

                String emoji;
                if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
                else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

                ResultSet result = Database.executeQuery("SELECT * FROM reactionRole WHERE messageID = '" + event.getOption("messageid", OptionMapping::getAsString) + "' AND emojiID = '" + emoji + "'");

                if (result.first()) {
                    Database.executeQuery("UPDATE reactionRole SET roleID = '" + event.getOption("role", OptionMapping::getAsRole).getId() + "' WHERE messageID = '" + event.getOption("messageid", OptionMapping::getAsString) + "' AND emojiID = '" + emoji + "'");
                    event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has replaced the already specified role for this emoji, and can now be obtained via the reaction role.").setEphemeral(true).queue();
                } else {
                    Database.executeQuery("INSERT INTO reactionRole(messageID, roleID, emojiID) VALUES ('" + event.getOption("messageid", OptionMapping::getAsString) + "','" + event.getOption("role", OptionMapping::getAsRole).getId() + "','" + emoji + "')");

                    TextChannel textChannel = event.getGuild().getTextChannelById(event.getOption("channel").getAsChannel().getId());
                    textChannel.retrieveMessageById(event.getOption("messageid").getAsString()).queue((message) -> message.addReaction(fromFormattedEmoji).queue());

                    event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " can now be obtained via the reaction role !").setEphemeral(true).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                Emoji.Type emojiType = fromFormattedEmoji.getType();

                String emoji;
                if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
                else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

                ResultSet result = Database.executeQuery("SELECT * FROM reactionRole WHERE messageID = '" + event.getOption("messageid", OptionMapping::getAsString) + "' AND emojiID = '" + emoji + "'");

                if (!result.first()) {
                    event.reply("No role has been configured for this emoji in the reaction role.").setEphemeral(true).queue();
                    return;
                }

                Database.executeQuery("DELETE FROM reactionRole WHERE messageID = '" + event.getOption("messageid", OptionMapping::getAsString) + "' AND emojiID = '" + emoji + "'");

                event.reply("THe role configured for this emoji in the reaction role has been cleared.").setEphemeral(true).queue();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
