package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReactionRoleListener extends ListenerAdapter {
    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        EmojiUnion fromFormattedEmoji = event.getEmoji();
        Emoji.Type emojiType = fromFormattedEmoji.getType();

        String emoji;
        if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
        else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            statement.setString(1, event.getMessageId());
            statement.setString(2, emoji);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String roleID = result.getString(2);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                assert member != null;

                event.getGuild().addRoleToMember(member, role).queue();
            }

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        Guild guild = event.getGuild();
        User member = event.getUser();

        EmojiUnion fromFormattedEmoji = event.getEmoji();
        Emoji.Type emojiType = fromFormattedEmoji.getType();

        String emoji;
        if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom().getId();
        else emoji = fromFormattedEmoji.asUnicode().getAsCodepoints();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM reactionRole WHERE messageID = ? AND emojiID = ?")) {
            statement.setString(1, event.getMessageId());
            statement.setString(2, emoji);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String roleID = result.getString(2);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                assert member != null;

                event.getGuild().removeRoleFromMember(member, role).queue();
            }

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
