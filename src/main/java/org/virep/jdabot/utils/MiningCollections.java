package org.virep.jdabot.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;


// TODO: Implement Database support
public class MiningCollections {
    private static JSONArray getCollection(String collectionName) throws Exception {
        InputStream is = MiningCollections.class.getResourceAsStream("/collections.json");

        assert is != null;
        JSONTokener tokener = new JSONTokener(is);
        JSONObject jsonObject = new JSONObject(tokener);

        if (!jsonObject.has(collectionName)) throw new Exception("Specified collection does not exist.");

        return jsonObject.getJSONArray(collectionName);
    }

    public static String getCollectionProgression(String collectionName) throws Exception {
        StringBuilder sb = new StringBuilder();

        JSONArray collection = getCollection(collectionName);
        int collectionAmt = 300;

        sb.append("The progression for the __**" + StringUtils.capitalize(collectionName) + "**__ collection.\n\n");

        for (int i = 0; i < collection.length(); i++) {
            if ((int)collection.get(i) <= collectionAmt) sb.append("\uD83D\uDFE9");
            else if ((int)collection.get(i) >= collectionAmt && (int)collection.get(i - 1) <= collectionAmt) sb.append("\uD83D\uDFE8");
            else sb.append("\uD83D\uDFE5");
        }

        sb.append("\n\nCurrent level: %collectionLevel% [%currentCollectionAmount%/%maxForCollectionLevel%]");

        return sb.toString();
    }
}
