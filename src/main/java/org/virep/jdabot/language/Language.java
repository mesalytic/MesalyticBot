package org.virep.jdabot.language;

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

public class Language {
    public static String getLanguage(Guild guild) {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM lang WHERE guildID = ?")) {
            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) return result.getString("lang");
            else return "en";
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
            return "en";
        }
    }

    public static String getString(String keyString, Guild guild) {
        InputStream in = Language.class.getResourceAsStream("/lang.json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        JSONObject langObject = jsonObject.getJSONObject(getLanguage(guild));

        return langObject.getString(keyString);
    }
}
