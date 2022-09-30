package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MathCommand implements Command {
    @Override
    public String getName() {
        return "math";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Interact with mathematical expressions.")
                .addSubcommands(
                        new SubcommandData("solve", "solve equations")
                                .addOption(OptionType.STRING, "equation", "the equation to solve", true),
                        new SubcommandData("parse", "parse equations")
                                .addOption(OptionType.STRING, "equation", "the equation to solve", true),
                        new SubcommandData("list", "List of built-in functions and operators")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (Objects.equals(event.getSubcommandName(), "list")) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("This menu is about the supported operators and functions for this command.\n\nSelect the content you want to see !")
                    .setTitle("Supported Operators & Functions")
                    .setColor(11868671)
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed).addActionRow(
                    SelectMenu.create("selectMenu:math")
                            .setMinValues(1)
                            .setPlaceholder("Main Menu")
                            .addOption("Operators", "selectMenu:math:operators")
                            .addOption("Functions", "selectMenu:math:functions")
                            .build()
            ).queue();
        } else if (Objects.equals(event.getSubcommandName(), "parse")) {
            event.deferReply().queue();
            String equation = Objects.requireNonNull(event.getOption("equation")).getAsString();

            String expression = URLEncoder.encode(equation, StandardCharsets.UTF_8);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            try {
                Request request = new Request.Builder()
                        .url("https://api.mesavirep.xyz/v1math?expression=" + expression)
                        .build();

                Response res = client.newCall(request).execute();

                assert res.body() != null;

                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(res.body()).string());
                String latexExpression = jsonObject.getString("body");

                TeXFormula latex = new TeXFormula(latexExpression);

                TeXIcon icon = latex.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);
                icon.setInsets(new Insets(2, 2, 2, 2));

                int width = icon.getIconWidth(), height = icon.getIconHeight();

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Graphics2D g2d = image.createGraphics();

                g2d.setColor(Color.white);
                g2d.fillRect(0, 0, width, height);

                icon.setForeground(Color.BLACK);
                icon.paintIcon(null, g2d, 0, 0);
                g2d.dispose();

                ImageIO.write(image, "png", baos);

                byte[] bytes = baos.toByteArray();

                FileUpload file = AttachedFile.fromData(bytes, "latex.png");

                event.getHook().editOriginalAttachments(file).queue();
            } catch (IOException e) {
                ErrorManager.handle(e, event);
            }
        }
        else
        {
            try {
                String equation = Objects.requireNonNull(event.getOption("equation")).getAsString();

                Expression expression = new ExpressionBuilder(equation).build();
                double result = expression.evaluate();

                event.reply("The result for that expression is: **" + result + "**").queue();
            } catch (UnknownFunctionOrVariableException | ArithmeticException e) {
                event.reply("The expression you specified is not valid.\n`" + e.getMessage() + "`\n\nIt must contains number, and supported operators and functions.\nPlease check the `/math list` command.").queue();
            }

        }
    }
}
