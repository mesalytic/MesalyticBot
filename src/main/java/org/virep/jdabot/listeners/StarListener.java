package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.virep.jdabot.Main;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StarListener extends ListenerAdapter {

    private final static Map<Long, Integer> starCount = new HashMap<>();
    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals("418433461817180180")) return;

        String starCodepoint = "U+2b50";

        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();

        if (reactionEmote.isEmoji()) {
            String emoji = reactionEmote.getAsCodepoints();

            if (emoji.equals(starCodepoint)) {
                try (PreparedStatement st = Main.connectionDB.prepareStatement("SELECT * FROM etoileUserReactedToMessages WHERE messageID = ? AND userID = ?")) {
                    st.setString(1, event.getMessageId());
                    st.setString(2, event.getUserId());

                    ResultSet result = st.executeQuery();

                    if (!result.first()) {
                        if (starCount.get(event.getMessageIdLong()) == null) {
                            starCount.put(event.getMessageIdLong(), 1);

                        } else {
                            starCount.replace(event.getMessageIdLong(), starCount.get(event.getMessageIdLong()) + 1);
                        }
                        try (PreparedStatement statement = Main.connectionDB.prepareStatement("""
                                    INSERT INTO etoileUserReactedToMessages(messageID, userID)
                                    VALUES (?, ?)
                                    """)
                        ) {
                            statement.setString(1, event.getMessageId());
                            statement.setString(2, event.getUserId());
                            statement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else return;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                int count = starCount.get(event.getMessageIdLong());
                if (count >= 3) {
                    try (PreparedStatement etoileUSERstatement = Main.connectionDB.prepareStatement("SELECT etoiles FROM etoileUSER WHERE userID = ?")) {
                        try (PreparedStatement etoileMESSAGEstatement = Main.connectionDB.prepareStatement("SELECT * FROM etoileMessages WHERE messageID = ?")) {
                            etoileUSERstatement.setString(1, event.getUserId());
                            etoileMESSAGEstatement.setString(1, event.getMessageId());

                            ResultSet resultSet1 = etoileUSERstatement.executeQuery();
                            ResultSet resultSet2 = etoileMESSAGEstatement.executeQuery();

                            if (!resultSet2.first()) {
                                if (resultSet1.first()) {
                                    try (PreparedStatement updateUSER = Main.connectionDB.prepareStatement("UPDATE etoileUSER SET etoiles = ? WHERE userID = ?")) {
                                        updateUSER.setInt(1, resultSet1.getInt(1) + 1);
                                        updateUSER.setString(2, event.getUserId());

                                        updateUSER.executeUpdate();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    try (PreparedStatement statement = Main.connectionDB.prepareStatement("""
                                    INSERT INTO etoileUSER(etoiles, userID)
                                    VALUES (?, ?)
                                    """)
                                    ) {
                                        statement.setInt(1, 1);
                                        statement.setString(2, event.getUserId());
                                        statement.executeUpdate();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                try (PreparedStatement statement = Main.connectionDB.prepareStatement("""
                                INSERT INTO etoileMessages(messageID)
                                VALUES (?)
                                """)
                                ) {
                                    statement.setString(1, event.getMessageId());
                                    statement.executeUpdate();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}