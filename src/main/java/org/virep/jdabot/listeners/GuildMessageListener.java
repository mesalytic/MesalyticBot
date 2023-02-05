package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildMessageListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM joinmessages WHERE guildID = ?");
             PreparedStatement statementDM = connection.prepareStatement("SELECT * FROM dmmessages WHERE guildID = ?")) {
            statement.setString(1, guild.getId());
            statementDM.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();
            ResultSet dmResult = statementDM.executeQuery();

            if (result.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

                assert channel != null;
                channel.sendMessage(message
                        .replace("%USER%", event.getMember().getAsMention())
                        .replace("%USERNAME%", event.getUser().getAsTag())
                        .replace("%SERVERNAME%", event.getGuild().getName())
                        .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))
                ).queue();
            }

            if (dmResult.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

                assert channel != null;
                channel.sendMessage(message
                        .replace("%USER%", event.getMember().getAsMention())
                        .replace("%USERNAME%", event.getUser().getAsTag())
                        .replace("%SERVERNAME%", event.getGuild().getName())
                        .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))
                ).addActionRow(Button.secondary("sentfromguild", "Sent from " + guild.getName()).asDisabled()).queue();
            }

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM leavemessages WHERE guildID = ?")) {
            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String message = result.getString(1);

                String channelID = result.getString(2);
                TextChannel channel = guild.getTextChannelById(channelID);

                assert channel != null;
                channel.sendMessage(message
                        .replace("%USERNAME%", event.getUser().getAsTag())
                        .replace("%SERVERNAME%", event.getGuild().getName())
                        .replace("%MEMBERCOUNT%", String.valueOf(event.getGuild().getMemberCount()))).queue();
            }
            connection.close();
        } catch (SQLException e) {
            ErrorManager.handleNoEvent(e);
        }
    }
}
