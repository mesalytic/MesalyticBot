package org.virep.jdabot.utils;

import net.dv8tion.jda.api.interactions.commands.Command;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// TODO: Implement Database support
public class MiningCollections {
    private static JSONArray getCollection(String collectionName) throws Exception {
        FileInputStream in = new FileInputStream("./collections.json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        if (!jsonObject.has(collectionName)) throw new Exception("Specified collection does not exist.");

        return jsonObject.getJSONArray(collectionName);
    }

    public static List<Command.Choice> getChoices() throws FileNotFoundException {
        List<Command.Choice> choices = new ArrayList<>();

        FileInputStream in = new FileInputStream("./collections.json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            choices.add(new Command.Choice(key, key));
        }

        return choices;
    }

    public static String getCollectionProgression(String collectionName, String userID) throws Exception {
        StringBuilder sb = new StringBuilder();

        JSONArray collection = getCollection(collectionName);

        sb.append("The progression for the __**").append(StringUtils.capitalize(collectionName)).append("**__ collection.\n\n");

        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT " + collectionName.toLowerCase() + " FROM miningCollectionsAmount WHERE userID = ?")) {
            try (PreparedStatement levelStatement = Main.connectionDB.prepareStatement("SELECT " + collectionName.toLowerCase() + " FROM miningCollectionsLevels WHERE userID = ?")) {
                levelStatement.setString(1, userID);
                statement.setString(1, userID);

                ResultSet resultAmount = statement.executeQuery();
                ResultSet levelAmount = levelStatement.executeQuery();

                if (resultAmount.next() && levelAmount.next()) {
                    int collectionIndex = resultAmount.findColumn(collectionName);
                    int collectionAmt = resultAmount.getInt(collectionIndex);


                    System.out.println(collectionAmt);
                    System.out.println(collection.length());

                    int level = levelAmount.getInt(collectionIndex);

                    for (int i = 1; i < collection.length(); i++) {
                        if (i == level + 1) sb.append("\uD83D\uDFE8");
                        else {
                            if ((int)collection.get(i) <= collectionAmt) sb.append("\uD83D\uDFE9");
                            else sb.append("\uD83D\uDFE5");
                        }
                    }
                    sb.append("\n\nCurrent level: %d [%d/%d]".formatted(level, collectionAmt, (int)collection.get(level)));
                }
            }
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
