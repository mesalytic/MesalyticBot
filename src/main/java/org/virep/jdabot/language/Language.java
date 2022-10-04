package org.virep.jdabot.language;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Language {
    public static Map<String, String> langs = new HashMap<>();

    public static void getLanguages() {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM lang")) {

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                setLanguage(result.getString("guildID"), result.getString("lang"));
            }
            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }

    public static void setLanguage(String guildID, String lang) {
        langs.put(guildID, lang);
    }

    public static String getLanguage(Guild guild) {
       return langs.getOrDefault(guild.getId(), "en");
    }

    public static String getString(String keyString, Guild guild) {
        InputStream in = Language.class.getResourceAsStream("/lang.json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        JSONObject langObject = jsonObject.getJSONObject(getLanguage(guild));

        if (langObject.has(keyString)) return langObject.getString(keyString);
        else return keyString;
    }
}
