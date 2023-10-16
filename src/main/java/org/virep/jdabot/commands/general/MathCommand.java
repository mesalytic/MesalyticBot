package org.virep.jdabot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MarkdownUtil;
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
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.Command;
import org.virep.jdabot.utils.ErrorManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Interagissez avec des expressions mathématiques")
                .addSubcommands(
                        new SubcommandData("solve", "Solve equations")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Résolvez des équations.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "equation", "The equation to solve. Use /math list to see the functions/operators.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'équation a résoudre. Utilisez /math list pour la liste des fonctions/signes.")
                                ),
                        new SubcommandData("parse", "Parse equations to LaTeX formatted images")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retranscrire une équation en une image LaTeX.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "equation", "The equation to parse", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'équation a retranscrire")
                                ),
                        new SubcommandData("list", "List of built-in functions and operators")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste des fonctions/signes disponibles")
                );
    }

    @Override
    public java.util.List<Permission> getBotPermissions() {
        List<Permission> permsList = new ArrayList<>();
        Collections.addAll(permsList, Permission.MESSAGE_EMBED_LINKS);

        return permsList;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (Objects.equals(event.getSubcommandName(), "list")) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription(Language.getString("MATH_LIST_EMBEDDESCRIPTION", guild))
                    .setTitle(Language.getString("MATH_LIST_EMBEDTITLE", guild))
                    .setColor(11868671)
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed).addActionRow(
                    StringSelectMenu.create("selectMenu:math")
                            .setMinValues(1)
                            .setPlaceholder("Main Menu")
                            .addOption(Language.getString("MATH_LIST_OPTION_OPERATORS", guild), "selectMenu:math:operators")
                            .addOption(Language.getString("MATH_LIST_OPTION_FUNCTIONS", guild), "selectMenu:math:functions")
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
                        .url("https://api.mesavirep.xyz/v1/math?expression=" + expression)
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

                event.getHook().editOriginal(MarkdownUtil.codeblock(latexExpression)).setFiles(file).queue();
            } catch (IOException e) {
                ErrorManager.handle(e, event);
            }
        } else {
            try {
                String equation = Objects.requireNonNull(event.getOption("equation")).getAsString();

                Expression expression = new ExpressionBuilder(equation).build();
                double result = expression.evaluate();

                event.reply(Language.getString("MATH_SOLVE_SOLVED", guild).replace("%RESULT%", String.valueOf(result))).queue();
            } catch (UnknownFunctionOrVariableException | ArithmeticException e) {
                event.reply(Language.getString("MATH_SOLVE_ERROR", guild).replace("%MESSAGE%", e.getMessage())).queue();
            }

        }
    }
}
