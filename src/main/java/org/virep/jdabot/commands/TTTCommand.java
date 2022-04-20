package org.virep.jdabot.commands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.util.Arrays;

public class TTTCommand extends SlashCommand {

    public TTTCommand() {
        super("tictactoe",
                "le tictactoe",
                new OptionData(OptionType.USER, "opponent", "Ping the opponent"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int[][] board = new int[3][3];

        System.out.println(Arrays.deepToString(board));

        StringBuilder replyContent = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) replyContent.append("\n");
            for (int j = 0; j < 3; j++) {
                System.out.println(i + " " + j);
                switch (board[i][j]) {
                    case 0 -> replyContent.append("\u2B1B");
                    case 1 -> replyContent.append("\u274C");
                    case 2 -> replyContent.append("\u2B55");
                }
            }
        }

        event.reply(replyContent.toString())
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
}
