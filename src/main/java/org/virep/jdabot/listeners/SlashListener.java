package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.Main;
import org.virep.jdabot.commands.games.TTTCommand;
import org.virep.jdabot.lavaplayer.AudioManagerController;
import org.virep.jdabot.lavaplayer.GuildAudioManager;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.slashcommandhandler.SlashHandler;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SlashListener extends ListenerAdapter {
    private final SlashHandler slashHandler;

    public SlashListener(SlashHandler slashHandler) {
        this.slashHandler = slashHandler;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Commands do not work in DMs.").queue();
            return;
        }
        String commandName = event.getName();
        Map<String, Command> commandMap = slashHandler.getSlashCommandMap();

        if (commandMap.containsKey(commandName)) {
            commandMap.get(commandName).execute(event);
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (Objects.equals(event.getSelectMenu().getId(), "selectMenu:logs:categoryEvents")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, Objects.requireNonNull(event.getGuild()).getId());

                ResultSet result = statement.executeQuery();
                ResultSetMetaData resultSetMetaData = result.getMetaData();

                if (result.first()) {
                    FileInputStream in = new FileInputStream("./logs.json");

                    JSONTokener tokener = new JSONTokener(in);
                    JSONObject logsObject = new JSONObject(tokener);

                    JSONArray logArray = logsObject.getJSONArray(event.getSelectedOptions().get(0).getValue().split(":")[3]);

                    List<SelectOption> moduleOptions = new ArrayList<>();
                    HashSet<String> defaultOptions = new HashSet<>();

                    for (int i = 0; i < logArray.length(); i++) {
                        JSONObject module = logArray.getJSONObject(i);

                        moduleOptions.add(SelectOption.of(module.getString("label"), "selectMenu:logs:events:" + module.getString("value")).withDescription(module.getString("description")));
                    }

                    for (int i = 1; i < resultSetMetaData.getColumnCount() - 1; i++) {
                        String modState = result.getString(i);

                        if (modState.equals("true"))
                            defaultOptions.add("selectMenu:logs:events:" + resultSetMetaData.getColumnName(i));
                    }

                    moduleOptions.forEach(mo -> System.out.println(mo.getLabel()));

                    event.editComponents().setComponents(
                            ActionRow.of(event.getSelectMenu().createCopy().setDefaultOptions(Collections.singleton(event.getSelectedOptions().get(0))).build()),
                            ActionRow.of(
                                    SelectMenu.create("selectMenu:logs:events")
                                            .addOptions(moduleOptions)
                                            .setDefaultValues(defaultOptions)
                                            .setMaxValues(moduleOptions.size())
                                            .setMinValues(0)
                                            .build()
                            )
                    ).queue();
                }
            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        if (Objects.equals(event.getSelectMenu().getId(), "selectMenu:logs:events")) {
            HashSet<String> options = new HashSet<>();

            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, Objects.requireNonNull(event.getGuild()).getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You must set up a log channel before. Use `/logs channel`").setEphemeral(true).queue();
                    return;
                }

                event.getSelectMenu().getOptions().forEach(option -> options.add(option.getValue().split(":")[3]));

                StringBuilder query = new StringBuilder();
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE logs SET ");

                for (String module : options) {
                    if (!event.getValues().isEmpty() && event.getValues().contains("selectMenu:logs:events:" + module))
                        query.append(module).append(" = \"true\", ");
                    else query.append(module).append(" = \"false\", ");
                }

                String builtQuery = query.toString();

                sb.append(builtQuery, 0, builtQuery.length() - 2);
                sb.append(" WHERE guildID = ?");

                try (PreparedStatement updateStatement = Main.connectionDB.prepareStatement(sb.toString())) {
                    updateStatement.setString(1, event.getGuild().getId());
                    updateStatement.executeUpdate();

                    event.reply("The (un)selected events have been successfully configured.").setEphemeral(true).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (event.getSelectMenu().getId().equals("selectMenu:math")) {
            String label = event.getSelectedOptions().get(0).getLabel();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(11868671)
                    .setTimestamp(Instant.now());

            if (label.equals("Operators")) {
                embedBuilder
                        .setDescription("""
                                __**Addition**__: `2 + 2`
                                __**Substraction**__: `2 - 2`
                                __**Multiplication**__: `2 * 2`
                                __**Division**__: `2 / 2`
                                __**Exponentation**__: `2 ^ 2`
                                __**Unary Minus/Plus (Sign Operators)**__: `+2 - (-2)`
                                __**Modulo**__: `2 % 2`
                                """)
                        .setTitle("Supported Operators");
            } else {
                embedBuilder
                        .setDescription("""
                                __**Absolute Value**__: `abs(x)`
                                __**Arc Cosine**__: `acos(x)`
                                __**Arc Sine**__: `asin(x)`
                                __**Arc Tangent**__: `atan(x)`
                                __**Cosine**__: `cos(x)`
                                __**Cubic Root**__: `cbrt(x)`
                                __**Euler's Number Raised to the power (e^x)**__: `exp(x)`
                                __**Hyperbolic Cosine**__: `cosh(x)`
                                __**Hyperbolic Sine**__: `sinh(x)`
                                __**Hyperbolic Tangent**__: `tanh(x)`
                                __**Logarithm (base 10)**__: `log1O(x)`
                                __**Logarithm (base 2)**__: `log2(x)`
                                __**Logarithmus Naturalis (base e)**__: `log(x, y)`
                                __**Nearest Upper Integer**__: `ceil(x)`
                                __**Nearest Lower Integer**__: `floor(x)`
                                __**Signum Function**__: `signum(x)`
                                __**Sine**__: `sin(x)`
                                __**Square Root**__: `sqrt(x)`
                                __**Tangent**__: `tan(x)`
                                """)
                        .setTitle("Supported Operators");
            }

            MessageEmbed embed = embedBuilder.build();

            event.getInteraction().editMessageEmbeds(embed).queue();
        }
    }

    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        Button button = event.getButton();

        if ("queueFull".equals(button.getId())) {
            GuildAudioManager guildAudioManager = AudioManagerController.getGuildAudioManager(event.getGuild());

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
                event.reply("You are not part of the game, you can't interact!").setEphemeral(true).queue();
                return;
            }
            event.getMessage().delete().queue();

            TTTCommand.playersTurn.put(event.getChannel().getIdLong(), TTTCommand.players.get(event.getChannel().getIdLong())[0]);
            TTTCommand.play(event, TTTCommand.playersTurn.get(event.getChannel().getIdLong()));
        }

        if ("tictactoeRefuse".equals(button.getId())) {
            if (Objects.requireNonNull(event.getMember()).getIdLong() != TTTCommand.players.get(event.getChannel().getIdLong())[1]) {
                event.reply("You are not part of the game, you can't interact!").setEphemeral(true).queue();
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
                event.reply("You are not part of the game!").setEphemeral(true).queue();
                return;
            }

            int[][] board = TTTCommand.boards.get(event.getChannel().getIdLong());
            long[] playersArray = TTTCommand.players.get(event.getChannel().getIdLong());

            if (TTTCommand.playersTurn.get(event.getChannel().getIdLong()) != event.getMember().getIdLong()) {
                event.reply("It's not your turn!").setEphemeral(true).queue();
                return;
            }

            switch (button.getId()) {
                case "tictactoeButton1" -> {
                    if (board[0][0] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[0][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton2" -> {
                    if (board[0][1] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[0][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton3" -> {
                    if (board[0][2] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[0][2] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton4" -> {
                    if (board[1][0] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[1][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton5" -> {
                    if (board[1][1] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[1][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton6" -> {
                    if (board[1][2] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[1][2] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton7" -> {
                    if (board[2][0] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[2][0] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton8" -> {
                    if (board[2][1] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
                        return;
                    }
                    board[2][1] = TTTCommand.playersTurn.get(event.getChannel().getIdLong()) == playersArray[0] ? 1 : 2;
                }
                case "tictactoeButton9" -> {
                    if (board[2][2] != 0) {
                        event.reply("That spot has already been played!").setEphemeral(true).queue();
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
