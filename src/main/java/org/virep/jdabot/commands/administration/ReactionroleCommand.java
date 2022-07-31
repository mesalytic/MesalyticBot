package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ReactionroleCommand implements Command {
    @Override
    public String getName() {
        return "reactionrole";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Configure roles that are given when clicking on a reaction.")
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
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
                EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                Emoji.Type emojiType = fromFormattedEmoji.getType();

                String emoji;
                if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
                else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

                statement.setString(1, event.getOption("messageid").getAsString());
                statement.setString(2, emoji);

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                UPDATE reactionRole SET roleID = ? WHERE messageID = ? AND emojiID = ?
                                """)) {
                        updateStatement.setString(1, Objects.requireNonNull(event.getOption("role")).getAsRole().getId());
                        updateStatement.setString(2, event.getOption("messageid").getAsString());
                        updateStatement.setString(3, emoji);

                        updateStatement.executeUpdate();
                        event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has replaced the already specified role for this emoji, and can now be obtained via the reaction role.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                INSERT INTO reactionRole(messageID, roleID, emojiID)
                                VALUES (?,?,?)
                                """)) {
                        updateStatement.setString(1, event.getOption("messageid").getAsString());
                        updateStatement.setString(2, Objects.requireNonNull(event.getOption("role")).getAsRole().getId());
                        updateStatement.setString(3, emoji);

                        TextChannel textChannel = event.getGuild().getTextChannelById(event.getOption("channel").getAsChannel().getId());

                        textChannel.retrieveMessageById(event.getOption("messageid").getAsString()).queue((message) -> message.addReaction(fromFormattedEmoji).queue());

                        updateStatement.executeUpdate();
                        event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " can now be obtained via the reaction role !").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
                EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                Emoji.Type emojiType = fromFormattedEmoji.getType();

                String emoji;
                if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
                else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

                statement.setString(1, event.getOption("messageid").getAsString());
                statement.setString(2, emoji);

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("No role has been configured for this emoji in the reaction role.").setEphemeral(true).queue();
                    return;
                }

                try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                DELETE FROM reactionRole WHERE messageID = ? AND emojiID = ?
                                """)) {
                    updateStatement.setString(1, Objects.requireNonNull(event.getOption("messageid")).getAsString());
                    updateStatement.setString(2, emoji);

                    updateStatement.executeUpdate();
                    event.reply("THe role configured for this emoji in the reaction role has been cleared.").setEphemeral(true).queue();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
