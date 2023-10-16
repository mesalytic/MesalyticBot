package org.virep.jdabot.commands.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.commands.games.ActivityCommand;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.handlers.SlashCommand;
import org.virep.jdabot.utils.ErrorManager;

import java.awt.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryCommand implements SlashCommand {
    @Override
    public String getName() {
        return "inventory";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Displays your Game Inventory");
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MESSAGE_EMBED_LINKS);

        return perms;
    }

    @Override
    public boolean isDev() {
        return true;
    }

    //TODO: Don't display resources the player doesn't have
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

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

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(String.format(Language.getString("INVENTORY_EMBED_GENERAL_TITLE", guild), event.getUser().getName()))
                    .setColor(Color.RED)
                    .addField("General", Language.getString("INVENTORY_EMBED_GENERAL_FIELD_ONE", guild).formatted(result.getInt("level"), result.getInt("xp"), 0, result.getInt("mana"), 100, result.getInt("hp"), 100), true)
                    .addField("Items", Language.getString("INVENTORY_EMBED_GENERAL_FIELD_ITEMS", guild).formatted(
                            itemObject.getJSONObject("pickaxe").getString(String.valueOf(result.getInt("pickaxe"))),
                            itemObject.getJSONObject("shovel").getString(String.valueOf(result.getInt("shovel"))),
                            itemObject.getJSONObject("axe").getString(String.valueOf(result.getInt("axe"))),
                            itemObject.getJSONObject("sword").getString(String.valueOf(result.getInt("sword"))),
                            itemObject.getJSONObject("shield").getString(String.valueOf(result.getInt("shield"))),
                            itemObject.getJSONObject("jetpack").getString(String.valueOf(result.getInt("jetpack"))),
                            itemObject.getJSONObject("summonable").getString(String.valueOf(result.getInt("summonable")))
                    ), false)
                    .setTimestamp(Instant.now())
                    .build();

            StringSelectMenu selectMenu = StringSelectMenu.create("selectMenu:inventoryMenu")
                    .addOption(Language.getString("INVENTORY_SELECTMENU_GENERAL_TITLE", guild), "selectMenu:inventoryMenu:general", Language.getString("INVENTORY_SELECTMENU_GENERAL_DESCRIPTION", guild))
                    .addOption(Language.getString("INVENTORY_SELECTMENU_EXTRACTED_TITLE", guild), "selectMenu:inventoryMenu:extracted", Language.getString("INVENTORY_SELECTMENU_EXTRACTED_DESCRIPTION", guild))
                    .addOption(Language.getString("INVENTORY_SELECTMENU_INDUSTRIAL_TITLE", guild), "selectMenu:inventoryMenu:industrial", Language.getString("INVENTORY_SELECTMENU_INDUSTRIAL_DESCRIPTION", guild))
                    .setMinValues(1)
                    .build();

            event.replyEmbeds(embed).addActionRow(selectMenu).queue();
        } catch (SQLException e) {
            ErrorManager.handle(e, event);
        }
    }
}
