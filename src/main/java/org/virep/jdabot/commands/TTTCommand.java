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
TODO: Check if its the player's turn when a button is pressed.

TODO: Check if someone wins each round.

*/

public class TTTCommand extends SlashCommand {

    public TTTCommand() {
        super("tictactoe",
                "le tictactoe",
                new OptionData(OptionType.USER, "opponent", "Ping the opponent"));
    }

    private static final int[][] board = new int[3][3];
    public static Map<Long, int[][]> boards = new HashMap<>();
    public static Map<Long, long[]> players = new HashMap<>();
    public static Map<Long, Long> playersTurn = new HashMap<>();
    public static long playerID;
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getOption("opponent") == null) {
            event.reply("Vous devez mentionner quelqu'un! `/tictactoe opponent`").setEphemeral(true).queue();
            return;
        }

        /*if (event.getOption("opponent").getAsUser().isBot()) {
            event.reply("Vous ne pouvez pas jouer contre un bot.").setEphemeral(true).queue();
            return;
        }*/

        if (boards.containsKey(event.getChannel().getIdLong())) event.reply("Une partie est deja en cours!").setEphemeral(true).queue();
        else {
            players.put(event.getChannel().getIdLong(), new long[]{Objects.requireNonNull(event.getMember()).getIdLong(), Objects.requireNonNull(event.getOption("opponent")).getAsUser().getIdLong() });

            User opponent = Objects.requireNonNull(event.getOption("opponent")).getAsUser();

            event.reply(opponent.getAsMention() + ", souhaitez vous jouer au morpion contre " + Objects.requireNonNull(event.getMember()).getAsMention() + " ?")
                    .addActionRow(
                            Button.primary("tictactoeAccept", "Accepter"),
                            Button.danger("tictactoeRefuse", "Refuser")
                    )
                    .queue();
        }
    }

    public static void play(ButtonInteractionEvent event, long playerID) {
        boards.put(event.getChannel().getIdLong(), board);

        long[] playersArray = players.get(event.getChannel().getIdLong());

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
        System.out.println(Arrays.deepToString(boardArray));

        StringBuilder replyContent = new StringBuilder();

        replyContent.append("<@").append(playerID).append(">, a toi de jouer!");

        for (int i = 0; i < 3; i++) {
            if (i > 0) replyContent.append("\n");
            for (int j = 0; j < 3; j++) {
                System.out.println(i + " " + j);
                switch (boardArray[i][j]) {
                    case 0 -> replyContent.append("\u2B1B");
                    case 1 -> replyContent.append("\u274C");
                    case 2 -> replyContent.append("\u2B55");
                }
            }
        }
        return replyContent.toString();
    }
}
