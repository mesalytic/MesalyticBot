package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.*;

/*
TODO: Translate all strings to English
*/

public class TTTCommand extends SlashCommand {

    public TTTCommand() {
        super("tictactoe",
                "Play TicTacToe with your friends!",
                new OptionData(OptionType.USER, "opponent", "Ping the opponent"));
    }

    private static final int[][] board = new int[3][3];
    public static final Map<Long, int[][]> boards = new HashMap<>();
    public static final Map<Long, long[]> players = new HashMap<>();
    public static final Map<Long, Long> playersTurn = new HashMap<>();

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getOption("opponent") == null) {
            event.reply("You must ping someone! `/tictactoe opponent`").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(event.getOption("opponent")).getAsUser() == event.getUser()) {
            event.reply("You cannot play against yourself!").setEphemeral(true).queue();
            return;
        }

        if (Objects.requireNonNull(event.getOption("opponent")).getAsUser().isBot()) {
            event.reply("You cannot play against a bot!").setEphemeral(true).queue();
            return;
        }

        if (boards.containsKey(event.getChannel().getIdLong())) event.reply("A game is already currently running.").setEphemeral(true).queue();
        else {
            players.put(event.getChannel().getIdLong(), new long[]{Objects.requireNonNull(event.getMember()).getIdLong(), Objects.requireNonNull(event.getOption("opponent")).getAsUser().getIdLong() });

            User opponent = Objects.requireNonNull(event.getOption("opponent")).getAsUser();

            event.reply(opponent.getAsMention() + ", would you like to play tictactoe against " + Objects.requireNonNull(event.getMember()).getAsMention() + " ?")
                    .addActionRow(
                            Button.primary("tictactoeAccept", "Accept"),
                            Button.danger("tictactoeRefuse", "Refuse")
                    )
                    .queue();
        }
    }

    public static void play(ButtonInteractionEvent event, long playerID) {
        boards.put(event.getChannel().getIdLong(), board);

        event.reply(replyBoard(board, playerID))
                .addActionRow(
                        Button.secondary("tictactoeButton1", Emoji.fromUnicode("\u0031\u20E3")),
                        Button.secondary("tictactoeButton2", Emoji.fromUnicode("\u0032\u20E3")),
                        Button.secondary("tictactoeButton3", Emoji.fromUnicode("\u0033\u20E3"))
                )
                .addActionRow(
                        Button.secondary("tictactoeButton4", Emoji.fromUnicode("\u0034\u20E3")),
                        Button.secondary("tictactoeButton5", Emoji.fromUnicode("\u0035\u20E3")),
                        Button.secondary("tictactoeButton6", Emoji.fromUnicode("\u0036\u20E3"))
                )
                .addActionRow(
                        Button.secondary("tictactoeButton7", Emoji.fromUnicode("\u0037\u20E3")),
                        Button.secondary("tictactoeButton8", Emoji.fromUnicode("\u0038\u20E3")),
                        Button.secondary("tictactoeButton9", Emoji.fromUnicode("\u0039\u20E3") )
                )
                .queue();
    }

    public static String replyBoard(int[][] boardArray, long playerID) {

        int checkWin = verifyWin(boardArray);

        StringBuilder replyContent = new StringBuilder();

        if (checkWin == 0) replyContent.append("<@").append(playerID).append(">, it's your turn!").append("\n\n");
        if (checkWin == 1 || checkWin == 2) replyContent.append("<@").append(playerID).append("> won!").append("\n\n");
        if (checkWin == 3) replyContent.append("Tie.").append("\n\n");

        for (int i = 0; i < 3; i++) {
            if (i > 0) replyContent.append("\n");
            for (int j = 0; j < 3; j++) {
                switch (boardArray[i][j]) {
                    case 0 -> replyContent.append("\u2B1B");
                    case 1 -> replyContent.append("\u274C");
                    case 2 -> replyContent.append("\u2B55");
                }
            }
        }
        return replyContent.toString();
    }

    public static int verifyWin(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) return board[i][0];
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) return board[0][i];
        }

        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) return board[0][0];
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) return board[0][2];

        int occ = 0;
        for (int[] ints : board) {
            for (int j = 0; j < board.length; j++) {
                if (ints[j] == 0) occ++;
            }
        }

        if (occ == 0) return 3;

        return 0;
    }
}
