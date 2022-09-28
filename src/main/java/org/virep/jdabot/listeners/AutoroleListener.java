package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoroleListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {

            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

                assert channel != null;
                channel.sendMessage(message
                        .replace("%USER%", event.getMember().getAsMention())
                        .replace("%USERNAME%", event.getUser().getAsTag())
                        .replace("%SERVERNAME%", event.getGuild().getName())
                        .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
            }
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
