package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.commands.games.ActivityCommand;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.utils.ErrorManager;

import java.awt.*;
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
    InputStream extractedInputStream = ActivityCommand.class.getResourceAsStream("/extractedResources.json");
    InputStream industrialInputStream = ActivityCommand.class.getResourceAsStream("/industrialResources.json");

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();

        if (Objects.equals(event.getSelectMenu().getId(), "selectMenu:logs:categoryEvents")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM logs WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

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
                                    StringSelectMenu.create("selectMenu:logs:events")
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
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("SELECTMENULISTENER_LOGS_NOCHANNEL", guild)).setEphemeral(true).queue();
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
                    statement2.setString(1, guild.getId());

                    statement2.executeUpdate();

                    event.reply(Language.getString("SELECTMENULISTENER_LOGS_CONFIGURED", guild)).setEphemeral(true).queue();
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
                        .setDescription(Language.getString("SELECTMENULISTENER_MATH_OPERATORS", guild))
                        .setTitle(Language.getString("SELECTMENULISTENER_MATH_OPTITLE", guild));
            } else {
                embedBuilder
                        .setDescription(Language.getString("SELECTMENULISTENER_MATH_FUNCTIONS", guild))
                        .setTitle(Language.getString("SELECTMENULISTENER_MATH_FNTITLE", guild));
            }

            MessageEmbed embed = embedBuilder.build();

            event.getInteraction().editMessageEmbeds(embed).queue();
        }

        if (event.getSelectMenu().getId().equals("selectMenu:inventoryMenu")) {
            String value = event.getSelectedOptions().get(0).getValue();
            MessageEmbed baseEmbed = event.getMessage().getEmbeds().get(0);
            MessageEmbed embed = null;

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now());

            if (value.equals("selectMenu:inventoryMenu:general")) {
                if (baseEmbed.getTitle().contains("General Information") || baseEmbed.getTitle().contains("Informations Générales")) embed = baseEmbed;
                else {
                    try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
                        statement.setString(1, event.getUser().getId());

                        ResultSet result = statement.executeQuery();

                        if (!result.first()) {
                            event.reply(Language.getString("INVENTORY_NOT_REGISTERED", guild)).queue();
                            return;
                        }
                        InputStream in = ActivityCommand.class.getResourceAsStream("/items.json");

                        JSONTokener tokener = new JSONTokener(in);
                        JSONObject itemObject = new JSONObject(tokener);

                        embedBuilder
                                .setTitle(String.format(Language.getString("INVENTORY_EMBED_GENERAL_TITLE", guild), event.getUser().getName()))
                                .addField("General", Language.getString("INVENTORY_EMBED_GENERAL_FIELD_ONE", guild).formatted(result.getInt("level"), result.getInt("xp"), 0, result.getInt("mana"), 100, result.getInt("hp"), 100), true)
                                .addField("Items", Language.getString("INVENTORY_EMBED_GENERAL_FIELD_ITEMS", guild).formatted(
                                        itemObject.getJSONObject("pickaxe").getString(String.valueOf(result.getInt("pickaxe"))),
                                        itemObject.getJSONObject("shovel").getString(String.valueOf(result.getInt("shovel"))),
                                        itemObject.getJSONObject("axe").getString(String.valueOf(result.getInt("axe"))),
                                        itemObject.getJSONObject("sword").getString(String.valueOf(result.getInt("sword"))),
                                        itemObject.getJSONObject("shield").getString(String.valueOf(result.getInt("shield"))),
                                        itemObject.getJSONObject("jetpack").getString(String.valueOf(result.getInt("jetpack"))),
                                        itemObject.getJSONObject("summonable").getString(String.valueOf(result.getInt("summonable")))
                                ), false);
                        embed = embedBuilder.build();

                    } catch (SQLException e) {
                        ErrorManager.handleNoEvent(e);
                    }
                }
            } else if (value.equals("selectMenu:inventoryMenu:extracted")) {
                if (baseEmbed.getTitle().contains("Extracted Resources") || baseEmbed.getTitle().contains("Ressources Basiques")) embed = baseEmbed;
                else {
                    try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
                        statement.setString(1, event.getUser().getId());

                        ResultSet result = statement.executeQuery();

                        if (!result.first()) {
                            event.reply(Language.getString("INVENTORY_NOT_REGISTERED", guild)).queue();
                            return;
                        }

                        embedBuilder.setTitle(String.format(Language.getString("INVENTORY_EMBED_EXTRACTED_TITLE", guild), event.getUser().getName()));
                        embedBuilder.addField(Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_WOOD_TITLE", guild), Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_WOOD_VALUE", guild).formatted(result.getInt("oak_wood"), result.getInt("birch_wood")), true);
                        embedBuilder.addField(Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_STONE_TITLE", guild), Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_STONE_VALUE", guild).formatted(result.getInt("cobblestone")), true);
                        embedBuilder.addField(Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_MINERALS_TITLE", guild), Language.getString("INVENTORY_EMBED_EXTRACTED_FIELD_MINERALS_VALUE", guild).formatted(
                                result.getInt("iron"),
                                result.getInt("gold"),
                                result.getInt("diamond"),
                                result.getInt("emerald"),
                                result.getInt("obsidian"),
                                result.getInt("copper"),
                                result.getInt("cobalt"),
                                result.getInt("aluminum"),
                                result.getInt("tin"),
                                result.getInt("ardite")
                        ), true);

                        embed = embedBuilder.build();
                    } catch (SQLException e) {
                        ErrorManager.handleNoEvent(e);
                    }

                }
            } else if (value.equals("selectMenu:inventoryMenu:industrial")) {
                if (baseEmbed.getTitle().contains("Industrial Resources") || baseEmbed.getTitle().contains("Ressources Industrielles")) embed = baseEmbed;
                else {
                    try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
                        statement.setString(1, event.getUser().getId());

                        ResultSet result = statement.executeQuery();

                        if (!result.first()) {
                            event.reply(Language.getString("INVENTORY_NOT_REGISTERED", guild)).queue();
                            return;
                        }

                        embedBuilder.setTitle(String.format(Language.getString("INVENTORY_EMBED_INDUSTRIAL_TITLE", guild), event.getUser().getName()));
                        embedBuilder.addField(Language.getString("INVENTORY_EMBED_INDUSTRIAL_FIELD_LIQUIDS_TITLE", guild), Language.getString("INVENTORY_EMBED_INDUSTRIAL_FIELD_LIQUIDS_VALUE", guild).formatted(result.getInt("oil")), true);
                        embedBuilder.addField(Language.getString("INVENTORY_EMBED_INDUSTRIAL_FIELD_ALLOY_TITLE", guild), Language.getString("INVENTORY_EMBED_INDUSTRIAL_FIELD_ALLOY_VALUE", guild).formatted(
                                result.getInt("manyullyn"),
                                result.getInt("bronze"),
                                result.getInt("alumite"),
                                result.getInt("aluminum_brass")
                        ), true);

                        embed = embedBuilder.build();
                    } catch (SQLException e) {
                        ErrorManager.handleNoEvent(e);
                    }
                }
            }

            event.getInteraction().editMessageEmbeds(embed).queue();
        }
    }
}
