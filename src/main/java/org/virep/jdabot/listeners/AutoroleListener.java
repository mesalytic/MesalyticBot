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
                String roleID = result.getString(1);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                guild.addRoleToMember(member, role).queue();
            }
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
