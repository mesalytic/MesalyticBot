package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class SelectMenuInteractionListener extends ListenerAdapter {
    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (Objects.equals(event.getSelectMenu().getId(), "selectMenu:logs:categoryEvents")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();
                ResultSetMetaData resultSetMetaData = result.getMetaData();

                if (result.first()) {
                    InputStream in = SlashListener.class.getResourceAsStream("/logs.json");

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
            } catch (SQLException e) {
                ErrorManager.handleNoEvent(e);
            }
        }
        if (Objects.equals(event.getSelectMenu().getId(), "selectMenu:logs:events")) {
            HashSet<String> options = new HashSet<>();

            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, event.getGuild().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You must set up a log channel before. Use `/logs channel`").setEphemeral(true).queue();
                    connection.close();
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

                try (PreparedStatement statement2 = connection.prepareStatement(sb.toString())) {
                    statement2.setString(1, event.getGuild().getId());

                    statement2.executeUpdate();

                    event.reply("The (un)selected events have been successfully configured.").setEphemeral(true).queue();
                }

                connection.close();
            } catch (SQLException e) {
                ErrorManager.handleNoEvent(e);
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
}
