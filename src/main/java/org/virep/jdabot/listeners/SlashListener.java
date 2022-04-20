package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.commands.TTTCommand;
import org.virep.jdabot.slashcommandhandler.SlashCommand;
import org.virep.jdabot.slashcommandhandler.SlashHandler;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class SlashListener extends ListenerAdapter {
    private final SlashHandler slashHandler;
    public SlashListener(SlashHandler slashHandler) {
        this.slashHandler = slashHandler;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Map<String, SlashCommand> commandMap = slashHandler.getSlashCommandMap();

        if (commandMap.containsKey(commandName)) {
            commandMap.get(commandName).execute(event);
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        Button button = event.getButton();
        System.out.println(event.getInteraction());

        if (Objects.equals(button.getId(), "tictactoeAccept")) {
            TTTCommand.playersTurn.put(event.getChannel().getIdLong(), TTTCommand.players.get(event.getChannel().getIdLong())[0]);
            TTTCommand.play(event, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
        }




        if (Objects.requireNonNull(button.getId()).startsWith("tictactoeButton")) {

            long playerOneID = TTTCommand.players.get(event.getChannel().getIdLong())[0];
            long playerTwoID = TTTCommand.players.get(event.getChannel().getIdLong())[1];

            if (Objects.requireNonNull(event.getMember()).getIdLong() != playerOneID || event.getMember().getIdLong() != playerOneID) {
                event.reply("Tu n'es pas de cette partie!").setEphemeral(true).queue();
                return;
            }

            int[][] board = TTTCommand.boards.get(event.getChannel().getIdLong());

            System.out.println("PlayerOneID " + playerOneID);
            System.out.println("PlayerTwoID " + playerTwoID);

            switch (button.getId()) {
                case "tictactoeButton1" -> board[0][0] = 1;
                case "tictactoeButton2" -> board[0][1] = 1;
                case "tictactoeButton3" -> board[0][2] = 1;
                case "tictactoeButton4" -> board[1][0] = 1;
                case "tictactoeButton5" -> board[1][1] = 1;
                case "tictactoeButton6" -> board[1][2] = 1;
                case "tictactoeButton7" -> board[2][0] = 1;
                case "tictactoeButton8" -> board[2][1] = 1;
                case "tictactoeButton9" -> board[2][2] = 1;
            }

            long[] playersArray = TTTCommand.players.get(event.getChannel().getIdLong());

            if (TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0]) {
                TTTCommand.playersTurn.remove(event.getChannel().getIdLong());
                TTTCommand.playersTurn.put(event.getChannel().getIdLong(), playersArray[1]);
            } else {
                TTTCommand.playersTurn.remove(event.getChannel().getIdLong());
                TTTCommand.playersTurn.put(event.getChannel().getIdLong(), playersArray[0]);
            }

            String replyBoard = TTTCommand.replyBoard(board, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
            event.editMessage(replyBoard).queue();
        }
    }
}
