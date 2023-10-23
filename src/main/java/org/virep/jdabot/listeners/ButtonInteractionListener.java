package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import org.virep.jdabot.commands.games.TTTCommand;
import org.virep.jdabot.commands.moderation.BansCommand;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.music.AudioManagerController;
import org.virep.jdabot.music.GuildAudioManager;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.virep.jdabot.utils.Utils.getPages;

public class ButtonInteractionListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        Button button = event.getButton();

        if (button.getId().equals("button:register:accept")) {

            event.getMessage().editMessage("""
                    **Rift Pulse: Rules of Engagement**
                                        
                    Welcome, esteemed Netrunner! Your unique identity has been etched into the annals of the virtual world. As you prepare to delve into the Ciphered Nexus, here are the rules you must abide by:
                                        
                    1. **Respect Others:** Treat your fellow Netrunners with courtesy and respect. Healthy discourse enriches our shared experience.
                    2. **No Spoilers:** Guard the secrets of the game. Avoid revealing in-game information or puzzle solutions to maintain the thrill for everyone.
                    3. **Fair Play:** Uphold the spirit of fair competition. Do not resort to cheats, hacks, or exploits. The challenge makes victory sweeter.
                    4. **Immerse Yourself:** Embrace the world of Rift Pulse. Develop a character, interact with NPCs, and explore the narrative depths.
                    5. **Have Fun:** Ultimately, this world is crafted for your enjoyment. Let your creativity thrive, and revel in the stories you create.
                                        
                    Your commitment to these principles ensures a vibrant and engaging experience for all Netrunners. Your journey begins now. Type `!help` to familiarize yourself with available commands.
                                        
                    May your code be elegant, and your exploits legendary. The digital realm eagerly anticipates your influence.
                                        
                    """).setComponents().queue();

            try (Connection connection = Database.getConnection(); PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO game_profile (userID) VALUES (?)")) {
                insertStatement.setString(1, event.getUser().getId());

                insertStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (button.getId().equals("button:register:cancel")) {
            event.getMessage().editMessage("""
                    **Rift Pulse: Registration**
                                        
                    Understood. If you change your mind and decide to join the adventure, simply type `!register` anytime.
                                        
                    Thank you for considering Rift Pulse: Chronicles of the Ciphered Nexus.
                    """).setComponents().queue();
        }

        if (button.getId().startsWith("button:bans")) {
            String[] labels = button.getId().split(":");

            if (labels[3].equals(event.getUser().getId())) {
                event.reply(Language.getString("BUTTONLISTENER_NOACCESS", guild)).setEphemeral(true).queue();
                return;
            }

            guild.retrieveBanList().queue(bans -> {
                Map<Long, Integer> pageNumbers = BansCommand.pageNumber;

                List<List<Guild.Ban>> banPages = getPages(bans, 50);

                int pageNumber = switch (labels[2]) {
                    case "first" -> 0;
                    case "last" -> banPages.size() - 1;
                    case "previous" -> pageNumbers.get(event.getChannel().getIdLong()) - 1;
                    case "next" -> pageNumbers.get(event.getChannel().getIdLong()) + 1;
                    default -> throw new IllegalStateException("Unexpected value");
                };

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getAvatarUrl())
                        .setTitle("Ban list for " + guild.getName())
                        .setFooter("Page " + (pageNumber + 1) + "/" + banPages.size())
                        .setTimestamp(Instant.now());

                List<Guild.Ban> page = banPages.get(pageNumber);

                StringBuilder embedDescription = new StringBuilder();

                page.forEach(ban -> {
                    embedDescription
                            .append("**")
                            .append(ban.getUser().getEffectiveName())
                            .append("** - ")
                            .append(ban.getReason())
                            .append("\n");
                });

                embedBuilder.setDescription(embedDescription.toString());

                pageNumbers.replace(event.getChannel().getIdLong(), pageNumber);

                event.editMessageEmbeds(embedBuilder.build()).queue();
            });
        }

        if ("queueFull".equals(button.getId())) {
            GuildAudioManager guildAudioManager = AudioManagerController.getGuildAudioManager(guild);

            StringBuilder queueBuilder = new StringBuilder();
            AtomicInteger counter = new AtomicInteger();

            guildAudioManager.getScheduler().queue.forEach(audioTrack -> {
                counter.getAndIncrement();
                queueBuilder
                        .append("\n")
                        .append("[")
                        .append(counter.get())
                        .append("] ")
                        .append(audioTrack.getInfo().getTitle())
                        .append(" - ")
                        .append(audioTrack.getInfo().getAuthor());
            });

            FileUpload file = AttachedFile.fromData(queueBuilder.toString().getBytes(), "queue.txt");

            event.replyFiles(file).setEphemeral(true).queue();
        }

        if ("tictactoeAccept".equals(button.getId())) {
            if (Objects.requireNonNull(event.getMember()).getIdLong() != TTTCommand.players.get(event.getChannel().getIdLong())[1]) {
                event.reply(Language.getString("BUTTONLISTENER_NOTINGAME", guild)).setEphemeral(true).queue();
                return;
            }
            event.getMessage().delete().queue();

            TTTCommand.playersTurn.put(event.getChannel().getIdLong(), TTTCommand.players.get(event.getChannel().getIdLong())[0]);
            TTTCommand.play(event, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
        }

        if ("tictactoeRefuse".equals(button.getId())) {
            if (Objects.requireNonNull(event.getMember()).getIdLong() != TTTCommand.players.get(event.getChannel().getIdLong())[1]) {
                event.reply(Language.getString("BUTTONLISTENER_NOTINGAME", guild)).setEphemeral(true).queue();
                return;
            }

            event.getMessage().delete().queue();
            event.reply("<@" + TTTCommand.players.get(event.getChannel().getIdLong())[0] + ">, <@" + TTTCommand.players.get(event.getChannel().getIdLong())[1] + "> refused to play!").queue();

            TTTCommand.boards.remove(event.getChannel().getIdLong());
            TTTCommand.players.remove(event.getChannel().getIdLong());
            TTTCommand.playersTurn.remove(event.getChannel().getIdLong());

            return;
        }

        if (Objects.requireNonNull(button.getId()).startsWith("tictactoeButton")) {

            long playerOneID = TTTCommand.players.get(event.getChannel().getIdLong())[0];
            long playerTwoID = TTTCommand.players.get(event.getChannel().getIdLong())[1];

            if (Objects.requireNonNull(event.getMember()).getIdLong() != playerOneID && event.getMember().getIdLong() != playerTwoID) {
                event.reply(Language.getString("BUTTONLISTENER_NOTINGAME", guild)).setEphemeral(true).queue();
                return;
            }

            int[][] board = TTTCommand.boards.get(event.getChannel().getIdLong());
            long[] playersArray = TTTCommand.players.get(event.getChannel().getIdLong());

            if (TTTCommand.playersTurn.get(event.getChannel().getIdLong()) != event.getMember().getIdLong()) {
                event.reply(Language.getString("BUTTONLISTENER_NOTYOURTURN", guild)).setEphemeral(true).queue();
                return;
            }

            switch (button.getId()) {
                case "tictactoeButton1" -> {
                    if (board[0][0] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[0][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton2" -> {
                    if (board[0][1] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[0][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton3" -> {
                    if (board[0][2] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[0][2] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton4" -> {
                    if (board[1][0] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[1][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton5" -> {
                    if (board[1][1] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[1][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton6" -> {
                    if (board[1][2] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[1][2] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton7" -> {
                    if (board[2][0] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[2][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton8" -> {
                    if (board[2][1] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[2][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton9" -> {
                    if (board[2][2] != 0) {
                        event.reply(Language.getString("BUTTONLISTENER_ALRPLAYED", guild)).setEphemeral(true).queue();
                        return;
                    }
                    board[2][2] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
            }

            int checkWin = TTTCommand.verifyWin(board);

            if (checkWin == 0) {
                if (TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0]) {
                    TTTCommand.playersTurn.remove(event.getChannel().getIdLong());
                    TTTCommand.playersTurn.put(event.getChannel().getIdLong(), playersArray[1]);
                } else {
                    TTTCommand.playersTurn.remove(event.getChannel().getIdLong());
                    TTTCommand.playersTurn.put(event.getChannel().getIdLong(), playersArray[0]);
                }
                String replyBoard = TTTCommand.replyBoard(board, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
                event.editMessage(replyBoard).queue();
            } else {
                String replyBoard = TTTCommand.replyBoard(board, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
                event.editMessage(replyBoard).setComponents().queue();

                TTTCommand.boards.remove(event.getChannel().getIdLong());
                TTTCommand.playersTurn.remove(event.getChannel().getIdLong());
                TTTCommand.players.remove(event.getChannel().getIdLong());
            }
        }
    }
}
